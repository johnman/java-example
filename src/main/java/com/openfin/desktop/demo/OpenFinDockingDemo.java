package com.openfin.desktop.demo;

import com.openfin.desktop.*;
import com.openfin.desktop.ActionEvent;
import com.openfin.desktop.System;
import com.openfin.desktop.Window;
import com.openfin.desktop.win32.ExternalWindowObserver;
import info.clearthought.layout.TableLayout;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Example of docking Java window with OpenFin html5 window
 *
 * clicking dock button to dock this app to the HTML5 app.
 *
 * Created by wche on 2/28/15.
 *
 */
public class OpenFinDockingDemo extends JPanel implements ActionListener, WindowListener {
    private final static Logger logger = LoggerFactory.getLogger(OpenFinDockingDemo.class.getName());


    private static JFrame jFrame;

    protected JButton launch;
    protected JButton close;

    protected JButton dockButtom, undockButton;

    protected ExternalWindowObserver externalWindowObserver;
    protected String javaWindowName = "Java Dock Window";
    protected String appUuid = "JavaDocking";
    protected String startupUuid = "OpenFinHelloWorld";

    protected DesktopConnection desktopConnection;
    protected SimpleDockingManager simpleDockingManager;

    protected JTextField dockStatus;  // show Ready to dock message
    protected JTextArea status;

    public OpenFinDockingDemo() {
        try {
            this.desktopConnection = new DesktopConnection(appUuid);
        } catch (DesktopException desktopError) {
            desktopError.printStackTrace();
        }
        setLayout(new BorderLayout());
        add(layoutCenterPanel(), BorderLayout.CENTER);
        add(layoutLeftPanel(), BorderLayout.WEST);
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        setMainButtonsEnabled(false);
        setAppButtonsEnabled(false);
    }

    private JPanel layoutLeftPanel() {
        JPanel panel = new JPanel();
        double size[][] = {{410}, {120, 30, TableLayout.FILL}};
        panel.setLayout(new TableLayout(size));
        panel.add(layoutActionButtonPanel(), "0,0,0,0");
        panel.add(layoutDockStatus(), "0,1,0,1");
        panel.add(layoutStatusPanel(), "0, 2, 0, 2");
        return panel;
    }

    private JTextField layoutDockStatus() {
        this.dockStatus = new JTextField();
        this.dockStatus.setForeground(Color.RED);
        return this.dockStatus;
    }

    private JPanel layoutActionButtonPanel() {
        JPanel buttonPanel = new JPanel();

        JPanel topPanel = new JPanel();
        double size[][] = {{10, 190, 20, 190, 10}, {25, 10, 25, 10}};
        topPanel.setLayout(new TableLayout(size));
        topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(2), "Desktop"));

        launch = new JButton("Launch OpenFin");
        launch.setActionCommand("start");
        close = new JButton("Shutdown OpenFin");
        close.setActionCommand("close");
        topPanel.add(launch, "1,0,1,0");
        topPanel.add(close, "3,0,3,0");

        dockButtom = new JButton("Dock to HTML5 app");
        dockButtom.setActionCommand("dock-window");
        dockButtom.setEnabled(false);
        topPanel.add(dockButtom, "1,2,1,2");

        undockButton = new JButton("Undock from HTML5 app");
        undockButton.setActionCommand("undock-window");
        undockButton.setEnabled(false);
        topPanel.add(undockButton, "3,2,3,2");

        close.addActionListener(this);
        launch.addActionListener(this);
        dockButtom.addActionListener(this);
        undockButton.addActionListener(this);

        buttonPanel.add(topPanel, "0,0");
        return buttonPanel;
    }

    private JPanel layoutCenterPanel() {
        JPanel panel = new JPanel();
        double size[][] = {{TableLayout.FILL}, {150, 150, TableLayout.FILL}};
        panel.setLayout(new TableLayout(size));

        return panel;
    }

    protected JPanel layoutStatusPanel() {
        //Simple status console
        status = new JTextArea();
        status.setEditable(false);
        status.setAutoscrolls(true);
        status.setLineWrap(true);
        JScrollPane statusPane = new JScrollPane(status);
        statusPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        statusPane.getViewport().setOpaque(false);
        statusPane.setOpaque(false);
        statusPane.setBorder(BorderFactory.createEmptyBorder(5,15,15,15));



        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(2), "Status"));
        panel.add(statusPane, BorderLayout.CENTER);

        return panel;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        closeDesktop();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    private void updateMessagePanel(final String msg) {
        if (SwingUtilities.isEventDispatchThread()) {
            String t = "";
            if (status.getText().length() > 0) {

                t = status.getText();
            }
            StringBuilder b = new StringBuilder();
            b.append(msg).append("\n").append(t);
            status.setText(b.toString());
            status.setCaretPosition(0);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateMessagePanel(msg);
                }
            });
        }
    }


    private void closeDesktop() {
        if (desktopConnection != null && desktopConnection.isConnected()) {
            try {
                externalWindowObserver.dispose();
                Thread.sleep(2000);
                new System(desktopConnection).exit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //closeWebSocket();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jFrame.dispose();
            }
        });
        try {
            Thread.sleep(1000);
            java.lang.System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void dockToStartupApp() {
        try {
            createNotification("Docking Java and HTML5");
            Window w = Window.wrap(startupUuid, javaWindowName, desktopConnection);
            w.joinGroup(Window.wrap(startupUuid, startupUuid, desktopConnection));
            dockButtom.setEnabled(false);
            undockButton.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotification(String text) throws Exception {
        NotificationOptions options = new NotificationOptions("http://demoappdirectory.openf.in/desktop/config/apps/OpenFin/HelloOpenFin/views/notification.html");
        options.setTimeout(5000);
        options.setMessageText(text);
        Notification notification = new Notification(options, new NotificationListener() {
            @Override
            public void onClick(Ack ack) {
                logger.info("notification onClick");
            }

            @Override
            public void onClose(Ack ack) {
                logger.info("notification onClose");
            }

            @Override
            public void onDismiss(Ack ack) {
                logger.info("notification onDismiss");
            }

            @Override
            public void onError(Ack ack) {
                logger.info("notification onError");
            }

            @Override
            public void onMessage(Ack ack) {
                logger.info("notification onMessage");
            }

            @Override
            public void onShow(Ack ack) {
                logger.info("notification onShow");
            }
        }, this.desktopConnection, null);
    }

    private void undockFromStartupApp() {
        try {
            createNotification("Undocking Java and HTML5");
            Window w = Window.wrap(startupUuid, javaWindowName, desktopConnection);
            this.simpleDockingManager.undock(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dockButtom.setEnabled(true);
        undockButton.setEnabled(false);
    }

    private void runStartAction() {
        try {
            DesktopStateListener listener = new DesktopStateListener() {
                @Override
                public void onReady() {
                    updateMessagePanel("Connection authorized.");
                    setMainButtonsEnabled(true);
                    simpleDockingManager = new SimpleDockingManager(desktopConnection);
                    registerJavaWindow();
                    launchHelloOpenFin();
                }

                @Override
                public void onError(String reason) {
                    updateMessagePanel("Connection failed: " + reason);
                }

                @Override
                public void onMessage(String message) {
                    updateMessagePanel("-->FROM DESKTOP-" + message);
                }

                @Override
                public void onOutgoingMessage(String message) {
                    updateMessagePanel("<--TO DESKTOP-" + message);
                }

            };
            desktopConnection.connectToVersion("stable", listener, 10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if ("start".equals(e.getActionCommand())) {
            runStartAction();
        } else if ("close".equals(e.getActionCommand())) {
            closeDesktop();
        } else if ("dock-window".equals(e.getActionCommand())) {
            dockToStartupApp();
        } else if ("undock-window".equals(e.getActionCommand())) {
            undockFromStartupApp();
        }
    }


    private void setMainButtonsEnabled(boolean enabled) {
        launch.setEnabled(!enabled);
        close.setEnabled(enabled);
    }

    private void setAppButtonsEnabled(boolean enabled) {
    }

    private void registerJavaWindow() {
        try {
            this.externalWindowObserver = new ExternalWindowObserver(desktopConnection.getPort(), startupUuid, javaWindowName, jFrame,
                    new AckListener() {
                        @Override
                        public void onSuccess(Ack ack) {
                            if (ack.isSuccessful()) {
                                try {
                                    dockButtom.setEnabled(true);
                                    undockButton.setEnabled(false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(Ack ack) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchHelloOpenFin() {
        ApplicationOptions options = new ApplicationOptions(startupUuid, startupUuid, "http://demoappdirectory.openf.in/desktop/config/apps/OpenFin/HelloOpenFin/index.html");
        options.setApplicationIcon("http://demoappdirectory.openf.in/desktop/config/apps/OpenFin/HelloOpenFin/img/openfin.ico");

        WindowOptions mainWindowOptions = new WindowOptions();
        mainWindowOptions.setAutoShow(false);
        mainWindowOptions.setDefaultHeight(525);
        mainWindowOptions.setDefaultLeft(10);
        mainWindowOptions.setDefaultTop(50);
        mainWindowOptions.setDefaultWidth(395);
        mainWindowOptions.setResizable(false);
        mainWindowOptions.setFrame(false);
        mainWindowOptions.setShowTaskbarIcon(true);
        options.setMainWindowOptions(mainWindowOptions);

        Application app = new Application(options, desktopConnection, new AckListener() {
            @Override
            public void onSuccess(Ack ack) {
                Application application = (Application) ack.getSource();
                try {
                    application.run();

                    simpleDockingManager.registerWindow(Window.wrap(startupUuid, javaWindowName, desktopConnection));
                    simpleDockingManager.registerWindow(Window.wrap(startupUuid, startupUuid, desktopConnection));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(Ack ack) {
            }
        });
    }

    private void showReadyToDockMessage(Window anchorWindow) {
        this.dockStatus.setText(String.format("Close to %s, release mouse to dock", anchorWindow.getName()));
    }
    private void clearReadyToDockMessage() {
        this.dockStatus.setText("");
    }
    private void updateUndockButton(boolean enabled) {
        this.undockButton.setEnabled(enabled);
    }


    private static void createAndShowGUI() {
        //Create and set up the window.
        jFrame = new JFrame("Java Docking Demo");
        jFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Create and set up the content pane.
        OpenFinDockingDemo newContentPane = new OpenFinDockingDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        jFrame.setContentPane(newContentPane);
        jFrame.addWindowListener(newContentPane);

        //Display the window.
        jFrame.pack();
        jFrame.setSize(470, 500);
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setVisible(true);
    }


    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


    /**
     * A simple manager that keep tracks of window positions and snap&dock windows if they are close enough
     *
     */
    class SimpleDockingManager {
        private DesktopConnection desktopConnection;
        private Map<String, WindowInfo> infoMap = new HashMap<String, WindowInfo>();  // stores bounds for all windows
        private boolean movingDuringDocking = false;
        private boolean delaySnap = false;   // if true, snap the windows on bounce-changed event (mouse release).
        private java.util.List<WindowInfo> dockCandidates = new ArrayList<WindowInfo>();  // stores 2 windows that are close enough to dock. Used when delaySnap is true
        private static final int DOCKING_DISTANCE = 20; // defines what's "close enough"

        public SimpleDockingManager(DesktopConnection desktopConnection) {
            this.desktopConnection = desktopConnection;
        }

        /**
         * Start keeping track of window movement
         *
         * @param window
         */
        public void registerWindow(final Window window) {
            final String key = WindowInfo.getInfoKey(window);
            if (this.infoMap.get(key) == null) {
                this.infoMap.put(key, new WindowInfo(window));
                window.getBounds(new AsyncCallback<WindowBounds>() {
                    public void onSuccess(WindowBounds result) {
                        WindowInfo info = infoMap.get(key);
                        info.bounds = result;
                        logger.debug("Got initial bounds for " + window.getName());
                    }
                }, null);
                window.addEventListener("bounds-changing", new EventListener() {
                    public void eventReceived(ActionEvent actionEvent) {
                    /*
                        // actionEvent.getEventObject() has following info
                        {
                            changeType: 2,  //describes what kind of change occurred.
                                            //0 means a change in position.
                                            //1 means a change in size.
                                            //2 means a change in position and size.
                            height: 2,      //the new height of the window.
                            left: 2,        //the left-most coordinate of the window.
                            name: "windowName", //(string) the name of the window.
                            top: 2,         //the top-most coordinate of the window.
                            topic: "window",
                            type: "bounds-changing",
                            uuid: "appUUID",//the UUID of the application the window belongs to.
                            width: 2        //the new width of the window.
                        }
                     */
                        boundsChanging(key, parseBounds(actionEvent.getEventObject()));
                    }
                }, null);
                window.addEventListener("bounds-changed", new EventListener() {
                    public void eventReceived(ActionEvent actionEvent) {
                    /*
                        // actionEvent.getEventObject() has following info
                        {
                            changeType: 2,  //describes what kind of change occurred.
                                            //0 means a change in position.
                                            //1 means a change in size.
                                            //2 means a change in position and size.
                            height: 2,      //the new height of the window.
                            left: 2,        //the left-most coordinate of the window.
                            name: "windowName", //(string) the name of the window.
                            top: 2,         //the top-most coordinate of the window.
                            topic: "window",
                            type: "bounds-changed",
                            uuid: "appUUID",//the UUID of the application the window belongs to.
                            width: 2        //the new width of the window.
                        }
                     */
                        boundsChanged(key, parseBounds(actionEvent.getEventObject()));
                    }
                }, null);
            }
        }

        private WindowBounds parseBounds(JSONObject data) {
            WindowBounds bounds = new WindowBounds(JsonUtils.getIntegerValue(data, "top", null),
                    JsonUtils.getIntegerValue(data, "left", null),
                    JsonUtils.getIntegerValue(data, "width", null),
                    JsonUtils.getIntegerValue(data, "height", null)
            );
            return bounds;
        }

        private void boundsChanging(String windowKey, WindowBounds bounds) {
            logger.debug("Bounds changing " + windowKey);
            WindowInfo windowInfo = this.infoMap.get(windowKey);
            windowInfo.bounds = bounds;
            if (windowInfo.anchorWindow == null) {
                if (!movingDuringDocking) {
                    this.dockCandidates.clear();
                    clearReadyToDockMessage();
                    Iterator<WindowInfo> infoIterator = this.infoMap.values().iterator();
                    while (infoIterator.hasNext()) {
                        WindowInfo another = infoIterator.next();
                        if (!another.equals(windowInfo)) {
                            WindowBounds movingBounds = shouldDock(windowInfo, another);
                            if (movingBounds != null) {
                                if (delaySnap) {
                                    this.dockCandidates.add(windowInfo);  // actual docking happens in boundsChanged
                                    this.dockCandidates.add(another);
                                    showReadyToDockMessage(another.window);
                                } else {
                                    dock(windowInfo, another, movingBounds);
                                }
                                break;
                            }
                        }
                    }
                }
            } else {
                // already docked
            }
        }

        private void boundsChanged(String windowKey, WindowBounds bounds) {
            logger.debug("Bounds changed " + windowKey);
            WindowInfo windowInfo = this.infoMap.get(windowKey);
            windowInfo.bounds = bounds;
            if (!movingDuringDocking) {
                if (this.dockCandidates.size() > 0) {
                    WindowInfo anchorInfo = this.dockCandidates.get(1);
                    WindowBounds movingBounds = shouldDock(windowInfo, anchorInfo);
                    if (movingBounds != null) {
                        dock(windowInfo, anchorInfo, movingBounds);
                    }
                    this.dockCandidates.clear();
                }
                clearReadyToDockMessage();
            }
        }


        /**
         * Decide if moving window should be docked to anchor window.  If yes, return bounds moving window should have
         *
         * @param movingInfo
         * @param anchorInfo
         * @return
         */
        private WindowBounds shouldDock(WindowInfo movingInfo, WindowInfo anchorInfo) {
            logger.debug(String.format("Checking shouldDock %s to %s", movingInfo.window.getName(), anchorInfo.window.getName()));
            WindowBounds movingBounds = null;
            if (movingInfo.anchorWindow == null || anchorInfo.anchorWindow == null) {
                WindowBounds bounds1 = movingInfo.bounds;
                WindowBounds bounds2 = anchorInfo.bounds;
                if (bounds1 != null && bounds2 != null) {
                    int bottom1 = bounds1.getTop() + bounds1.getHeight();
                    int bottom2 = bounds2.getTop() + bounds2.getHeight();
                    int right1 = bounds1.getLeft() + bounds1.getWidth();
                    int right2 = bounds2.getLeft() + bounds2.getWidth();
                    if (bounds1.getLeft() < right2 && bounds2.getLeft() < right1) {
                        // two are in top-bottom relationship
                        if (Math.abs(bounds1.getTop() - bottom2) < DOCKING_DISTANCE) {
                            movingBounds = new WindowBounds(movingInfo.bounds.getTop(), movingInfo.bounds.getLeft(), 0, 0);
                            movingBounds.setTop(bottom2);
                            logger.debug(String.format("Detecting bottom-top docking %s to %s", movingInfo.window.getName(), anchorInfo.window.getName()));
                        }
                        else if (Math.abs(bounds2.getTop() - bottom1) < DOCKING_DISTANCE) {
                            movingBounds = new WindowBounds(movingInfo.bounds.getTop(), movingInfo.bounds.getLeft(), 0, 0);
                            movingBounds.setTop(bounds2.getTop() - bounds1.getHeight());
                            logger.debug(String.format("Detecting top-bottom docking %s to %s", movingInfo.window.getName(), anchorInfo.window.getName()));
                        } else {
                            logger.debug(String.format("shouldDock %s to %s too far top-bottom", movingInfo.window.getName(), anchorInfo.window.getName()));
                        }
                    } else if (bounds1.getTop() < bottom2 && bounds2.getTop() < bottom1) {
                        // two are in side-by-side relationship
                        if (Math.abs(bounds1.getLeft() - right2) < DOCKING_DISTANCE) {
                            movingBounds = new WindowBounds(movingInfo.bounds.getTop(), movingInfo.bounds.getLeft(), 0, 0);
                            movingBounds.setLeft(right2);
                            logger.debug(String.format("Detecting right-left docking %s to %s", movingInfo.window.getName(), anchorInfo.window.getName()));
                        }
                        else if (Math.abs(bounds2.getLeft() - right1) < DOCKING_DISTANCE) {
                            movingBounds = new WindowBounds(movingInfo.bounds.getTop(), movingInfo.bounds.getLeft(), 0, 0);
                            movingBounds.setLeft(bounds2.getLeft() - bounds1.getWidth());
                            logger.debug(String.format("Detecting left-right docking %s to %s", movingInfo.window.getName(), anchorInfo.window.getName()));
                        }
                    } else {
                        logger.debug(String.format("shouldDock %s to %s not overlapping", movingInfo.window.getName(), anchorInfo.window.getName()));
                    }
                }
            }
            return movingBounds;
        }

        /**
         * Dock 2 windows. Snap moving window to still window first
         * @param movingInfo moving window
         * @param anchorInfo  still window
         * @param newBounds  new bounds for moving window
         */
        private void dock(final WindowInfo movingInfo, final WindowInfo anchorInfo, WindowBounds newBounds) {
            logger.debug(String.format("Docking %s to %s", movingInfo.window.getName(), anchorInfo.window.getName()));
            // snap moving window
            if (anchorInfo.anchorWindow == null) {
                anchorInfo.anchorWindow = anchorInfo.window;
            }
            movingInfo.anchorWindow = anchorInfo.window;
            try {
                movingDuringDocking = true;
                logger.debug(String.format("Moving %s to %d %d", movingInfo.window.getName(), newBounds.getTop(), newBounds.getLeft()));
                movingInfo.window.moveTo(newBounds.getLeft(), newBounds.getTop(), new AckListener() {
                    public void onSuccess(Ack ack) {
                        movingDuringDocking = false;
                        if (ack.isSuccessful()) {
                            movingInfo.window.joinGroup(anchorInfo.window, new AckListener() {
                                public void onSuccess(Ack ack) {
                                    updateUndockButton(true);
                                }
                                public void onError(Ack ack) {
                                }
                            });
                        }
                    }
                    public void onError(Ack ack) {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void undock(Window window) {
            WindowInfo windowInfo = this.infoMap.get(WindowInfo.getInfoKey(window));
            if (windowInfo != null) {
                try {
                    if (windowInfo.anchorWindow != null) {
                        window.leaveGroup();
                        if (windowInfo.anchorWindow != null) {
                            WindowInfo anchorInfo = this.infoMap.get(WindowInfo.getInfoKey(windowInfo.anchorWindow));
                            anchorInfo.anchorWindow = null;
                        }
                        windowInfo.anchorWindow = null;
                        updateUndockButton(false);
                    }
                } catch (Exception ex) {
                    logger.error("Error leaving group", ex);
                }
            }
        }

        /**
         * Dock 2 windows with moving them together
         *
         * @param window
         * @param anchorWindow
         */
        public void dock(Window window, Window anchorWindow) {
            registerWindow(window);
            registerWindow(anchorWindow);
            dock(this.infoMap.get(WindowInfo.getInfoKey(window)), this.infoMap.get(WindowInfo.getInfoKey(anchorWindow)), null);
        }
    }

    private static class WindowInfo {
        public Window window;
        public Window anchorWindow;  // group leader if this.window is docked
        public WindowBounds bounds;

        public WindowInfo(Window window) {
            this.window = window;
        }
        public static String getInfoKey(Window window) {
            return window.getUuid() + ":" + window.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WindowInfo that = (WindowInfo) o;
            return getInfoKey(window).equals(getInfoKey(that.window));
        }
        @Override
        public int hashCode() {
            return getInfoKey(window).hashCode();
        }
    }
}
