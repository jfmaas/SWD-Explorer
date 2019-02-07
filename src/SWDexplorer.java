import SWDdata.SWDatabase;
import SWDgui.SWDgui;
import SWDio.SWDlogger;

class SWDexplorer {
    public static void main(String[] args) {
        SWDlogger.setOutfile("SWDlog.log");
        SWDatabase content = new SWDatabase();
        SWDgui gui = new SWDgui(content);
        gui.createGUI();
    }
}
