/**
 * Created by firejox on 2015/10/31.
 */




import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


import java.awt.*;

import java.awt.event.*;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.LinkedTransferQueue;


public class EventDispatcher extends Thread {
    final double cycle = 1.0/60.0;
    xWindow win;
    xEventReservoir evt_res;
    Vector<xEvent> evt_list;
    long timestamp;

    EventDispatcher () {
        evt_res = new xEventReservoir();
        win = new xWindow("window", evt_res);
        evt_list = new Vector<xEvent>();
        win.setVisible(true);
        timestamp = System.currentTimeMillis();
    }

    public void run() {
        while (true) {
            evt_res.flush(evt_list);

            for (xEvent evt : evt_list) {
                String s = evt.event_info();

                if (s != null)
                    System.out.println(s);
            }

            evt_list.clear();

            long cur_t = System.currentTimeMillis();
            if ((cur_t - timestamp)  < (cycle * 1000)) {
                try {
                    sleep(timestamp - cur_t + (long) (cycle * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timestamp = System.currentTimeMillis();

        }
    }

    static public void main (String args[]) {
        EventDispatcher ed = new EventDispatcher();
        ed.start();

    }
}

class xWindow extends JFrame {
    String name;
    xScreen s[];

    xWindow (String w_name, xEventReservoir res, String title, GraphicsConfiguration gc) {
        super (title, gc);
        name = w_name;
        construct_context(res);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    xWindow (String w_name, xEventReservoir res, String title) {
        this (w_name, res, title, null);
    }

    xWindow (String w_name, xEventReservoir res, GraphicsConfiguration gc) {
        this (w_name, res, null, gc);
    }

    xWindow (String w_name, xEventReservoir res) {
        this (w_name, res, null, null);
    }


    private void construct_context(xEventReservoir res) {
        this.setMinimumSize(new Dimension(300, 400));
        this.getContentPane().setLayout(new GridBagLayout());

        s = new xScreen[10];

        GridBagConstraints gbc = new GridBagConstraints();
        JPanel tmp = new JPanel(new GridLayout(1, 1));
        tmp.setBorder(new EmptyBorder(10, 10, 10, 10));

        s[1] = new xScreen("screen 1", res);

        tmp.add(s[1]);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 7;
        gbc.gridheight = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        this.getContentPane().add(tmp, gbc);

        tmp = new JPanel(new GridLayout(1, 1));
        tmp.setBorder(new EmptyBorder(10, 10, 10, 10));

        s[9] = new xScreen("screen 9", res);

        tmp.add(s[9]);

        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.25;

        this.getContentPane().add(tmp, gbc);

        tmp = new JPanel(new GridLayout(1, 1));
        tmp.setBorder(new EmptyBorder(10, 10, 10, 10));

        s[8] = new xScreen("screen 8", res);

        tmp.add(s[8]);

        gbc.gridx = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 0.75;

        this.getContentPane().add(tmp, gbc);

        tmp = new JPanel(new GridLayout(2, 3, 5, 5));
        tmp.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (int  i = 2; i <= 7; i++) {
            s[i] = new xScreen("screen " + Integer.toString(i), res);

            tmp.add(s[i]);
        }

        gbc.gridx = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;

        this.getContentPane().add(tmp, gbc);


    }
}


class xScreen extends JPanel  {
    String name;
    JLabel label;

    xScreen (String scr_name, xEventReservoir res, LayoutManager layout, boolean dbuf_flag) {
        super(layout, dbuf_flag);

        name = scr_name;

        this.addKeyListener(res);
        this.addMouseListener(res);
        this.addMouseMotionListener(res);

        this.setBorder(new LineBorder(Color.BLACK, 5));

        label = new JLabel(name);
        this.add(label);
    }

    xScreen (String scr_name, xEventReservoir res, LayoutManager layout) {
        this(scr_name, res, layout, true);
    }

    xScreen (String scr_name, xEventReservoir res, boolean dbuf_flag) {
        this(scr_name, res, new FlowLayout(), dbuf_flag);
    }

    xScreen (String scr_name, xEventReservoir res) {
        this(scr_name, res, true);
    }

}



class xKeyEvent extends xEvent {

    final protected xEventType evt_type = xEventType.KEY_EVT;
    KeyEvent evt;

    xKeyEvent (KeyEvent ev) {
        evt = ev;

    }

    public String event_info () {
        xScreen scr = (xScreen)evt.getSource();

        switch (evt.getID()) {
            case KeyEvent.KEY_PRESSED:
                return scr.name + ": keyboard input";

            default:
                return null;
        }

    }

}


class xMouseEvent extends xEvent {


    final protected xEventType evt_type = xEventType.MOUSE_EVT;
    MouseEvent evt;

    xMouseEvent (MouseEvent ev) {
        evt = ev;
    }

    public String event_info () {
        xScreen scr = (xScreen) evt.getSource();

        switch (evt.getID()) {
            case MouseEvent.MOUSE_CLICKED:
                return scr.name + ": mouse click";

            case MouseEvent.MOUSE_PRESSED:
                return scr.name + ": mouse down";

            case MouseEvent.MOUSE_RELEASED:
                return scr.name + ": mouse up";

            case MouseEvent.MOUSE_DRAGGED:
                return scr.name + ": mouse drag";

            default:
                return null;
        }
    }

}

enum xEventType {
    KEY_EVT,
    MOUSE_EVT
}

abstract class xEvent {
    protected xEventType evt_type;

    xEvent() {
        evt_type = null;
    }

    public abstract String event_info ();

    public xEventType get_event_type () {
        return evt_type;
    }

}

final class xEventReservoir implements KeyListener,
        MouseListener, MouseMotionListener {

    LinkedTransferQueue<xEvent> evt_queue;

    xEventReservoir() {
        evt_queue = new LinkedTransferQueue<xEvent>();
    }

    private void transfer_key_event (KeyEvent key_ev) {
        try {
            evt_queue.transfer(new xKeyEvent(key_ev));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void transfer_mouse_event (MouseEvent mouse_ev) {
        try {
            evt_queue.transfer(new xMouseEvent(mouse_ev));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void keyTyped (KeyEvent e) {
        transfer_key_event(e);
    }

    public void keyPressed (KeyEvent e) {
        transfer_key_event(e);
    }

    public void keyReleased (KeyEvent e) {
        transfer_key_event(e);
    }

    public void mouseExited (MouseEvent e) {
        transfer_mouse_event(e);
    }

    public void mouseEntered (MouseEvent e) {
        transfer_mouse_event(e);
    }

    public void mousePressed (MouseEvent e) {
        ((xScreen)e.getSource()).requestFocusInWindow();
        transfer_mouse_event(e);
    }

    public void mouseReleased (MouseEvent e) {
        transfer_mouse_event(e);
    }

    public void mouseClicked (MouseEvent e) {
        transfer_mouse_event(e);
    }

    public void mouseDragged (MouseEvent e) {
        transfer_mouse_event(e);
    }

    public void mouseMoved (MouseEvent e) {
        transfer_mouse_event(e);
    }

    public void flush(Collection<xEvent> evts) {
        evt_queue.drainTo(evts);

    }

}