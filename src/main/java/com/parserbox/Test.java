package com.parserbox;

import net.sourceforge.tess4j.*;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Test {
  //  public String TEST_DIR = "c:/home/jrosario/test";
    public String TEST_DIR = "c:/test";

    public static void main(String[] args) {
        new Test().runTest();
    }


    public void runTest() {
        try {
            String filename = getUniqueName();
            PDDocument document = PDDocument.load(new File(TEST_DIR + File.separator + "AP-hist2.pdf"));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = 0;
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                String fileName = TEST_DIR + File.separator + filename + page + ".png";
                ImageIOUtil.writeImage(bim, fileName, 300);
                pageCount++;
            }
            document.close();

            Tesseract tesseract = new Tesseract();
           // tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
            tesseract.setDatapath("C:/hold/Tess4J-3.4.8-src/Tess4J/tessdata");

            ITessAPI.TessResultRenderer renderer = new ITessAPI.TessResultRenderer();
            List<ITesseract.RenderedFormat> list = new ArrayList<>();
            list.add(ITesseract.RenderedFormat.PDF);

            for (int i = 0; i < pageCount; ++i) {
                tesseract.createDocuments(
                        TEST_DIR + File.separator + filename + i + ".png",
                        TEST_DIR + File.separator + filename + i,
                        list);
            }

            PDFMergerUtility mergerUtility = new PDFMergerUtility();
            mergerUtility.setDestinationFileName(TEST_DIR + File.separator + filename + "_final.pdf");

            for (int i = 0; i < pageCount; ++i) {
                File file = new File(TEST_DIR + File.separator + filename + i + ".pdf");
                mergerUtility.addSource(file);
            }
            mergerUtility.mergeDocuments(null);

            for (int i = 0; i < pageCount; ++i) {
                File f1 = new File(TEST_DIR + File.separator + filename + i + ".png");
                File f2 = new File(TEST_DIR + File.separator + filename + i + ".pdf");
                FileUtils.forceDelete(f1);
                FileUtils.forceDelete(f2);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized String getUniqueName() {
        String prefix = "pdoc" + "_" + new Date().getTime() + "_";
        return prefix;
    }
    public static int generateRandomIntIntRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
