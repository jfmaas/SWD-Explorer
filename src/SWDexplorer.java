import SWDdata.SWDdatabase;
import SWDgui.SWDgui;
import SWDio.SWDlogger;

class SWDexplorer {
    public static void main(String[] args) {
        SWDlogger.setOutfile("SWDlog.log");
        SWDdatabase content = new SWDdatabase();
        SWDgui gui = new SWDgui(content);
        gui.createGUI();
    }
}
