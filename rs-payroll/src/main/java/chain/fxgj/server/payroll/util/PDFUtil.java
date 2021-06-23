package chain.fxgj.server.payroll.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.*;

@Slf4j
public class PDFUtil {
    public static final String CHARACTOR_FONT_CH_FILE = "Kaiti.ttf";
//    public static final String CHARACTOR_FONT_CH_FILE = "/Users/admin/project/fxgj/fxgj/rs-fxgj/src/env/dev/FZLTCXHJW.TTF";

    public static final Rectangle PAGE_SIZE = PageSize.A4;
    public static final float MARGIN_LEFT = 50;
    public static final float MARGIN_RIGHT = 50;
    public static final float MARGIN_TOP = 50;
    public static final float MARGIN_BOTTOM = 50;
    public static final float SPACING = 20;


    private Document document = null;
    private PdfWriter writer = null;

    /**
     * 功能：创建导出数据的目标文档
     * @param fileName 存储文件的临时路径
     * @return
     */
    public void createDocument(String fileName, PdfPageEvent pdfPageEvent) {
        File file = new File(fileName);
        document = new Document(PAGE_SIZE, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
        try {
            writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        } catch (Exception e) {
            log.error("error",e);
            e.printStackTrace();
        }
        if(pdfPageEvent!=null){
            writer.setPageEvent(pdfPageEvent);
        }
        // 打开文档准备写入内容
        document.open();
    }

    /**
     * 将章节写入到指定的PDF文档中
     * @param chapter
     * @return
     */
    public void writeChapterToDoc(Chapter chapter) {
        try {
            if(document != null) {
                if(!document.isOpen()) document.open();
                document.add(chapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 功能  创建PDF文档中的章节
     * @param title 章节标题
     * @param chapterNum 章节序列号
     * @param alignment 0表示align=left，1表示align=center
     * @param numberDepth 章节是否带序号 设值=1 表示带序号 1.章节一；1.1小节一...，设值=0表示不带序号
     * @param font 字体格式
     * @return Chapter章节
     */
    public static Chapter createChapter(String title, int chapterNum, int alignment, int numberDepth, Font font) {
        Paragraph chapterTitle = new Paragraph(title, font);
        chapterTitle.setAlignment(alignment);
        Chapter chapter = new Chapter(chapterTitle, chapterNum);
        chapter.setNumberDepth(numberDepth);
        return chapter;
    }

    /**
     * 功能：创建某指定章节下的小节
     * @param chapter 指定章节
     * @param title 小节标题
     * @param font 字体格式
     * @param numberDepth 小节是否带序号 设值=1 表示带序号 1.章节一；1.1小节一...，设值=0表示不带序号
     * @return section在指定章节后追加小节
     */
    public static Section createSection(Chapter chapter, String title, Font font, int numberDepth) {
        Section section = null;
        if(chapter != null) {
            Paragraph sectionTitle = new Paragraph(title, font);
            sectionTitle.setSpacingBefore(SPACING);
            section = chapter.addSection(sectionTitle);
            section.setNumberDepth(numberDepth);
        }
        return section;
    }

    /**
     * 功能：向PDF文档中添加的内容
     * @param text 内容
     * @param font 内容对应的字体
     * @return phrase 指定字体格式的内容
     */
    public static Phrase createPhrase(String text, Font font) {
        Phrase phrase = new Paragraph(text,font);
        return phrase;
    }

    /**
     * 功能：创建列表
     * @param numbered  设置为 true 表明想创建一个进行编号的列表
     * @param lettered 设置为true表示列表采用字母进行编号，为false则用数字进行编号
     * @param symbolIndent
     * @return list
     */
    public static List createList(boolean numbered, boolean lettered, float symbolIndent) {
        List list = new List(numbered, lettered, symbolIndent);
        return list;
    }

    /**
     * 功能：创建列表中的项
     * @param content 列表项中的内容
     * @param font 字体格式
     * @return listItem
     */
    public static ListItem createListItem(String content, Font font) {
        ListItem listItem = new ListItem(content, font);
        return listItem;
    }

    /**
     * 功能：创造字体格式
     * @param fontname
     * @param size 字体大小
     * @param style 字体风格
     * @param color 字体颜色
     * @return Font
     */
    public static Font createFont(String fontname, float size, int style, BaseColor color) {
        Font font =  FontFactory.getFont(fontname, size, style, color);
        return font;
    }

    /**
     * 功能： 返回支持中文的字体---仿宋
     * @param size 字体大小
     * @param style 字体风格
     * @param color 字体 颜色
     * @return  字体格式
     */
    public static Font createCHineseFont(float size, int style, BaseColor color) {
        BaseFont bfChinese = null;
        try {
            bfChinese = BaseFont.createFont(CHARACTOR_FONT_CH_FILE,BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font(bfChinese, size, style, color);
    }

    /**
     * 最后关闭PDF文档
     */
    public void closeDocument() {
        if(document != null) {
            document.close();
        }
    }


    /**
     * 读PDF文件，使用了pdfbox开源项目
     * @param fileName
     */
    public void readPDF(String fileName) {
        File file = new File(fileName);
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            // 新建一个PDF解析器对象
            PDFParser parser = new PDFParser(in);
            // 对PDF文件进行解析
            parser.parse();
            // 获取解析后得到的PDF文档对象
            PDDocument pdfdocument = parser.getPDDocument();
            // 新建一个PDF文本剥离器
            PDFTextStripper stripper = new PDFTextStripper();
            // 从PDF文档对象中剥离文本
            String result = stripper.getText(pdfdocument);
            System.out.println("PDF文件的文本内容如下：");
            System.out.println(result);

        } catch (Exception e) {
            System.out.println("读取PDF文件" + file.getAbsolutePath() + "生失败！" + e);
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    // 设置table样式
    public void decorateTable(PdfPTable table) {
        table.setHeaderRows(1);
        // table.setFooterRows(1);
        table.setComplete(true);
        // table.setSkipFirstHeader(true);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);
        table.setSpacingBefore(10);
    }

    // 创建table头
    public PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(7);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new BaseColor(200, 219, 234));
        return cell;
    }

    // 创建table 列
    public PdfPCell createCell(String text, Font font) {
        if (text == null)
            text = " ";
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER); //水平居中
        return cell;
    }

    public void addImage(String imagePath,float x,float y,float fitWidth,float fitHeight){
        try {
            // 读图片
            Image image = Image.getInstance(imagePath);
            // 获取操作的页面
            //PdfContentByte under = stamper.getOverContent(6);
            // 根据域的大小缩放图片
            image.scaleToFit(fitWidth, fitHeight);
            // 添加图片
            image.setAbsolutePosition(x, y);
            //under.addImage(image);

            document.add(image);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 测试pdf文件的创建
     * @param args
     */
    public static void main(String[] args) throws IOException, BadElementException {

        String fileName = "/Users/admin/Downloads/test11.pdf";  //这里先手动把绝对路径的文件夹给补上。
        PDFUtil pdfUtil = new PDFUtil();

        Font chapterFont = PDFUtil.createCHineseFont(20, Font.BOLD, new BaseColor(0, 0, 255));//文章标题字体
        Font sectionFont = PDFUtil.createCHineseFont(16, Font.BOLD, new BaseColor(0, 0, 255));//文章小节字体
        Font textFont = PDFUtil.createCHineseFont(10, Font.NORMAL, new BaseColor(0, 0, 0));//小节内容字体

        //ImageEvent imageEvent=new ImageEvent();
        pdfUtil.createDocument(fileName,null);
        Chapter chapter = PDFUtil.createChapter("糖尿病病例1", 1, 1, 0, chapterFont);
        Section section1 = PDFUtil.createSection(chapter, "病例联系人信息", sectionFont,0);
        Phrase text1 = PDFUtil.createPhrase("如您手中有同类现成病例，在填写完以上基础信息后，传病例附件",textFont);
        section1.add(text1);

        Section section2 = PDFUtil.createSection(chapter, "病例个人体会", sectionFont,0);
        Phrase text2 = PDFUtil.createPhrase("1.下载病例生成PDF文档",textFont);
//      text2.setFirstLineIndent(20);  //第一行空格距离
        section2.add(text1);
        section2.add(text2);

        List list = PDFUtil.createList(true, false, 20);
        String tmp = "还有什么能够文档。文档是 PDF 文档的所有元素的容器。 ";
        ListItem listItem1 = PDFUtil.createListItem(tmp,textFont);
        ListItem listItem2 = PDFUtil.createListItem("列表2",textFont);
        list.add(listItem1);
        list.add(listItem2);
        section2.add(list);

        Section section3 = PDFUtil.createSection(chapter, "表格", sectionFont,0);
        float[] widths = {0.15f, 0.21f,0.15f,0.21f,0.24f};
        PdfPTable table1 = new PdfPTable(widths);
        pdfUtil.decorateTable(table1);
        table1.addCell(pdfUtil.createHeaderCell("收款人",textFont));
        table1.addCell(pdfUtil.createCell("111",textFont));
        table1.addCell(pdfUtil.createHeaderCell("收款人证件号",textFont));
        table1.addCell(pdfUtil.createCell("11111",textFont));
        table1.addCell(pdfUtil.createHeaderCell("电子回单专用章",textFont));

        table1.addCell(pdfUtil.createHeaderCell("发放机构",textFont));
        table1.addCell(pdfUtil.createCell("111",textFont));
        table1.addCell(pdfUtil.createHeaderCell("收款账户号",textFont));
        table1.addCell(pdfUtil.createCell("11111",textFont));
        PdfPCell pdfCell = new PdfPCell();
        pdfCell.setImage(Image.getInstance("/Users/admin/Downloads/huaxhuid.png"));//插入图片
        pdfCell.setRowspan(6);
        table1.addCell(pdfCell);

        table1.addCell(pdfUtil.createHeaderCell("资金类型",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));
        table1.addCell(pdfUtil.createHeaderCell("方案名称",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));

        table1.addCell(pdfUtil.createHeaderCell("方案编号",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));
        table1.addCell(pdfUtil.createHeaderCell("资金月份",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));

        table1.addCell(pdfUtil.createHeaderCell("付款账户名称",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));
        table1.addCell(pdfUtil.createHeaderCell("付款账户号",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));

        table1.addCell(pdfUtil.createHeaderCell("交易金额",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));
        table1.addCell(pdfUtil.createHeaderCell("发放时间",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));

        table1.addCell(pdfUtil.createHeaderCell("付款结果",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));
        table1.addCell(pdfUtil.createHeaderCell("备注",textFont));
        table1.addCell(pdfUtil.createCell("0",textFont));
        section3.add(table1);


        pdfUtil.writeChapterToDoc(chapter);
//        pdfUtil.addImage("/Users/admin/Downloads/huaxhuid.png",400,100,100,100);

        pdfUtil.closeDocument();
    }

    public class ImageEvent extends PdfPageEventHelper{
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                // 读图片
                Image image = Image.getInstance("/Users/admin/Downloads/huaxhuid.png");
                // 获取操作的页面
                //PdfContentByte under = stamper.getOverContent(6);
                // 根据域的大小缩放图片
                image.scaleToFit(100, 100);
                // 添加图片
                image.setAbsolutePosition(400, 100);
                //under.addImage(image);

                document.add(image);

                Font textFont = PDFUtil.createCHineseFont(8, Font.NORMAL, new BaseColor(0, 0, 0));
                ColumnText.showTextAligned(writer.getDirectContent(),
                        Element.ALIGN_CENTER, new Phrase("- "+writer.getPageNumber()+" -",textFont),
                        (document.rightMargin() + document.right()+ document.leftMargin() - document.left() - 7) / 2.0F + 20F,
                        document.bottom() - 20, 0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
