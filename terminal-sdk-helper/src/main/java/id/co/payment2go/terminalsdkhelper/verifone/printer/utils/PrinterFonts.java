package id.co.payment2go.terminalsdkhelper.verifone.printer.utils;

import android.content.res.AssetManager;
import android.os.Environment;

/**
 * Created by Simon on 2018/6/1.
 */

public class PrinterFonts {
    public static final String FONT_AGENCYB = "fonts/AGENCYB.TTF";
    public static final String FONT_ALGER = "fonts/ALGER.TTF";
    public static final String FONT_BROADW = "fonts/BROADW.TTF";
    public static final String FONT_CURLZ___ = "fonts/CURLZ___.TTF";
    public static final String FONT_FORTE = "fonts/FORTE.TTF";
    public static final String FONT_KUNSTLER = "fonts/KUNSTLER.TTF";
    public static final String FONT_segoesc = "fonts/segoescb.ttf";
    public static final String FONT_SHOWG = "fonts/SHOWG.TTF";
    public static final String FONT_WINGDNG2 = "WINGDNG2.TTF";
    public static final String FONT_HuaWenLiShu = "STLITI.TTF";
    public static final String FONT_HuaWenZhongSong = "STZHONGS.TTF";

    public static final String FONT_MONTSERRAT_REGULAR = "fonts/MONTSERRAT_REGULARS.TTF";

    public static String path = "";

    public static void initialize( AssetManager assets ) {
        String fileName = PrinterFonts.FONT_AGENCYB;
        path = Environment.getExternalStorageDirectory().getPath().concat("/fonts/");
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_ALGER;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_BROADW;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_CURLZ___;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_FORTE;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_KUNSTLER;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_segoesc;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_SHOWG;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_WINGDNG2;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_HuaWenLiShu;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_HuaWenZhongSong;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

        fileName = PrinterFonts.FONT_MONTSERRAT_REGULAR;
        ExtraFiles.copy("fonts/" + fileName, path , fileName, assets, false );

    }

}
