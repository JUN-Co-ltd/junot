package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.component.PurchaseItemCreatePdfComponent;
import jp.co.jun.edi.component.PurchaseItemCreateXmlComponent;
import jp.co.jun.edi.component.mail.MailAddressComponent;
import jp.co.jun.edi.component.mail.MailSenderAttachmentComponent;
import jp.co.jun.edi.component.model.CommonPropertyModel;
import jp.co.jun.edi.component.model.MailAttachementFileModel;
import jp.co.jun.edi.component.model.TemporaryFileForPdfGenerationModel;
import jp.co.jun.edi.entity.MMailTemplateEntity;
import jp.co.jun.edi.entity.TPurchaseEntity;
import jp.co.jun.edi.entity.TPurchasesVoucherEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.MMailTemplateRepository;
import jp.co.jun.edi.repository.TPurchaseFileInfoRepository;
import jp.co.jun.edi.repository.TPurchaseRepository;
import jp.co.jun.edi.repository.TPurchasesVoucherRepository;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.MessageCodeType;
//PRD_0179 #10654 add JEF start
import jp.co.jun.edi.type.PurchaseType;
//PRD_0179 #10654 add JEF end
import jp.co.jun.edi.type.SendMailStatusType;
import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

//PRD_0134 #10654 add JEF start
/**
 * 仕入明細メール送信コンポーネント.
 */
@Slf4j
@Component
public class PurchaseItemScheduleComponent {
    /** PDFファイル.ファイル名.種別(JR：仕入明細). */
    private static final String PDF_FILENAME_TYPE_JR = "JR";

    @Autowired
    private PropertyComponent propertyComponent;

    @Autowired
    private ScheduleBusinessComponent scheduleBusinessComponent;

    @Autowired
    private TPurchasesVoucherRepository tPurchasesVoucherRepository;

    @Autowired
    private TPurchaseRepository tPurchaseRepository;

    @Autowired
    private PurchaseItemCreateXmlComponent createXmlComponent;

    @Autowired
    private PurchaseItemCreatePdfComponent createPdfComponent;

    @Autowired
    private MailAddressComponent mailAddressComponent;

    @Autowired
    private MMailTemplateRepository mMailTemplateRepository;

    @Autowired
    private MailSenderAttachmentComponent mailSenderAttachmentComponent;

    @Autowired
    private TPurchaseFileInfoRepository tPurchaseFileInfoRepository;
    /**
     *
     * 仕入伝票作成処理の実行.
     * @param list 仕入伝票管理情報
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final List<TPurchasesVoucherEntity> list) {

    	final TPurchasesVoucherEntity firstEntity = list.get(0);
        log.info(LogStringUtil.of("execute")
                .message("Start processing of ReturnItemSchedule.")
                .build());
        // 一時フォルダに生成されたXMLファイル、PDFファイル格納用
        final List<TemporaryFileForPdfGenerationModel> files = new ArrayList<>();
        // PDF送信した場合に、仕入ファイル情報のステータス更新用の伝票No格納リスト
        final List<String> voucherNumbers = new ArrayList<>();
        final BigInteger userId = scheduleBusinessComponent.getUserId();
        list.stream().forEach(TPurchasesVoucherEntity -> {
        try {

        	final TemporaryFileForPdfGenerationModel file = new TemporaryFileForPdfGenerationModel();
        	// PRD_0179 #10654 add JEF start
        	//PRD_0202 mod JFE start
//        	final TPurchaseEntity tPurchaseEntity = tPurchaseRepository.findByVoucherNumAndVoucherLine(
//        	        TPurchasesVoucherEntity.getPurchaseVoucherNumber(), TPurchasesVoucherEntity.getPurchaseVoucherLine()).orElseThrow(
//        	                () -> new ResourceNotFoundException(
//        	                        ResultMessages.warning().add(
//        	                                MessageCodeType.CODE_002, LogStringUtil.of("execute")
//        	                                        .message("t_purchase not found.")
//        	                                        .value("purchase_voucher_number", TPurchasesVoucherEntity.getPurchaseVoucherNumber())
//        	                                        .value("purchase_voucher_line", TPurchasesVoucherEntity.getPurchaseVoucherLine())
//        	                                        .build())));
        	final TPurchaseEntity tPurchaseEntity = tPurchaseRepository.findByVoucherNumAndVoucherLineAndOrderId(
                    TPurchasesVoucherEntity.getPurchaseVoucherNumber(), TPurchasesVoucherEntity.getPurchaseVoucherLine(), TPurchasesVoucherEntity.getOrderId()).orElseThrow(
        	                () -> new ResourceNotFoundException(
        	                        ResultMessages.warning().add(
        	                                MessageCodeType.CODE_002, LogStringUtil.of("execute")
        	                                        .message("t_purchase not found.")
        	                                        .value("purchase_voucher_number", TPurchasesVoucherEntity.getPurchaseVoucherNumber())
        	                                        .value("purchase_voucher_line", TPurchasesVoucherEntity.getPurchaseVoucherLine())
                                                    .value("order_id", TPurchasesVoucherEntity.getOrderId())
        	                                        .build())));
        	//PRD_0202 mod JFE end
        	// PRD_0179 #10654 add JEF end
            // プロパティ情報取得
            // 一時フォルダ
            final String temporayFolder = getTemporaryPath(TPurchasesVoucherEntity.getId());
            // XMLファイルパス
            final Path xmlPath = generatedXmlFilePath(temporayFolder, TPurchasesVoucherEntity.getPurchaseVoucherNumber(), TPurchasesVoucherEntity.getOrderId());
            // PDFファイルパス
            final Path pdfPath = generatedPdfFilePath(temporayFolder, TPurchasesVoucherEntity.getPurchaseVoucherNumber(), TPurchasesVoucherEntity.getOrderId());
            // XSLファイルパス
            // PRD_0179 #10654 mod JEF start
            //final Path xslPath = getXslPath();
            final Path xslPath;
            if (tPurchaseEntity.getPurchaseType() == PurchaseType.RETURN_PURCHASE) {
                xslPath = getReturnXslPath();
            } else {
                xslPath = getXslPath();
            }
            // PRD_0179 #10654 mod JEF end
            // PDFファイル名
        	//PRD_0202 mod JFE start
            //final String fileName = generatedPDFFileName(TPurchasesVoucherEntity.getPurchaseVoucherNumber(), TPurchasesVoucherEntity.getPurchaseVoucherLine());
        	final String fileName = generatedPDFFileName(TPurchasesVoucherEntity.getPurchaseVoucherNumber(), TPurchasesVoucherEntity.getPurchaseVoucherLine(), TPurchasesVoucherEntity.getOrderId());
            //PRD_0202 mod JFE end

            // XML作成
            boolean sendPDF = createXmlComponent.createXml(TPurchasesVoucherEntity.getPurchaseVoucherNumber(), TPurchasesVoucherEntity.getOrderId(),
            		TPurchasesVoucherEntity.getCreatedAt(), xmlPath);

            // PDF作成
            final String voucherNumber = TPurchasesVoucherEntity.getPurchaseVoucherNumber();
            final BigInteger ordrId = TPurchasesVoucherEntity.getOrderId();
            createPdfComponent.createPdf(userId, voucherNumber, ordrId, xslPath, xmlPath, pdfPath, fileName);
            // 一時フォルダのXMLファイル、PDFファイルを削除
            //PRD_0173 #10654 jfe mod start
            //deleteFile(temporayFolder, xmlPath, pdfPath);
            //PRD_0173 #10654 jfe mod end

            // 一時フォルダ
            file.setTemporayFolder(temporayFolder);
            // XMLファイルパス
            file.setXmlFilePath(xmlPath);
            // PDFファイルパス
            file.setPdfFilePath(pdfPath);
            // PDFファイル名
            file.setFileName(fileName);

            // filesに登録（この時に消化委託の場合はセットしたくない。ヘッダー情報取るときに合わせて仕入区分と入荷場所を取ってきて判断する。)
            if (sendPDF == true) {
                files.add(file);
                voucherNumbers.add(voucherNumber);
                }
            // ステータスを 処理完了 に更新
            updateStatus(SendMailStatusType.COMPLETED, TPurchasesVoucherEntity.getId(), userId);
        } catch (ResourceNotFoundException e) {
            log.warn(e.getMessage(), e);
            // ステータスを 処理済み、かつ、警告あり に更新
            updateStatus(SendMailStatusType.COMPLETED_WARN, TPurchasesVoucherEntity.getId(), userId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // ステータスを エラー に更新
            updateStatus(SendMailStatusType.ERROR, TPurchasesVoucherEntity.getId(), userId);
        }
        });

        //ここにメール送信？

        if (!files.isEmpty()) {
        	//メール送信
        	sendMail(files,firstEntity,voucherNumbers,userId);
			//PRD_0172 #10654 jfe add start
			files.stream().forEach(TemporaryFileForPdfGenerationModel->{
				deleteFile(TemporaryFileForPdfGenerationModel.getTemporayFolder(),
						TemporaryFileForPdfGenerationModel.getXmlFilePath(), 
						TemporaryFileForPdfGenerationModel.getPdfFilePath());
			});
			//PRD_0172 #10654 jfe add end
        }

        log.info(LogStringUtil.of("execute")
                .message("End processing of ReturnItemSchedule.")
                .build());

    }

    /**
     * 一時ディレクトリを取得する.
     * ディレクトリ構成：{一時ディレクトリ}/returnItem/{ユーザID}/
     * @param userId ユーザID
     * @return 発注別の一時ディレクトリ
     * @throws Exception 例外
     */
    private String getTemporaryPath(final BigInteger userId) throws Exception {
        String tmpDirectory = propertyComponent.getBatchProperty().getScheduleFopTemporaryFolder();
        if (!tmpDirectory.endsWith("/")) {
            // 接尾辞がスラッシュでない場合、接尾辞にスラッシュをつける
            tmpDirectory = tmpDirectory.concat("/");
        }
        tmpDirectory = tmpDirectory.concat("purchaseItem/").concat(userId.toString()).concat("/");
        Files.createDirectories(Paths.get(tmpDirectory));

        return tmpDirectory;
    }

    /**
     * XMLファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param purchaseVoucherNumber 伝票番号
     * @param orderId 発注ID
     * @return XMLファイルパス
     */
    private Path generatedXmlFilePath(final String path, final String purchaseVoucherNumber, final BigInteger orderId) {
        // XMLファイル purchase_item_{伝票番号}_{発注ID}_{yyyyMMddHHmmssSSS}.xml
        final String nowDate = DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssS");
        final String xmlFile = "purchase_item_" + purchaseVoucherNumber + "_" + orderId.toString() + "_" + nowDate + ".xml";
        return Paths.get(path + xmlFile);
    }

    /**
     * PDFファイルパスを生成.
     *
     * @param path 一時フォルダ
     * @param purchasevoucherNumber 伝票番号
     * @param orderId 発注ID
     * @return XMLファイルパス
     */
    private Path generatedPdfFilePath(final String path, final String purchasevoucherNumber, final BigInteger orderId) {
        // PDFファイル purchase_item_{伝票番号}_{発注ID}_{yyyyMMddHHmmssSSS}.xml
        final String nowDate = DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssS");
        final String pdfFile = "purchase_item_" + purchasevoucherNumber + "_" + orderId.toString() + "_" + nowDate + ".pdf";

        return Paths.get(path + pdfFile);
    }

    /**
     * XSLファイルパスを取得.
     * @return XSLファイルパス
     * @throws Exception 例外
     */
    private Path getXslPath() throws Exception {
		//PRD_0151 mod JFE start
//		final String strXslPath = "C:\\Users\\KIT76807\\Desktop\\JUN\\web_edi_api-staging\\config\\pdf\\xsl\\purchase-item.xsl";
		final String strXslPath = propertyComponent.getBatchProperty().getPurchaseItemPathXsl();
		//PRD_0151 mod JFE start
        final Path xslPath = Paths.get(strXslPath);
        if (!Files.exists(xslPath)) {
            throw new ScheduleException(LogStringUtil.of("getXslPath")
                    .message("not find path.")
                    .value("purchase-item.path.xsl", strXslPath)
                    .build());
        }
        return xslPath;
    }

    // PRD_0179 #10654 add JEF start
    /**
     * XSLファイルパスを取得.(返品用)
     * @return XSLファイルパス
     * @throws Exception 例外
     */
    private Path getReturnXslPath() throws Exception {
        final String strXslPath = propertyComponent.getBatchProperty().getPurchaseDigestionReturnPathXsl();
        final Path xslPath = Paths.get(strXslPath);
        if (!Files.exists(xslPath)) {
            throw new ScheduleException(LogStringUtil.of("getReturnXslPath")
                    .message("not find path.")
                    .value("purchase-digestion-return.path.xsl", strXslPath)
                    .build());
        }
        return xslPath;
    }
    // PRD_0179 #10654 add JEF end

    /**
     * 仕入明細PDFファイル名 生成.
     *
     * 品番CHAR(8)-種別CHAR(2)発注番号CHAR(8)種別CHAR(2)
     * 例）ANM59050-JK100012.pdf
     *
     * @param purchasevoucherNumber 伝票番号
     * @param purchaseVoucherLine 仕入伝票行
     * @return PDF名
     */
      //PRD_0202 mod JFE start
//    private String generatedPDFFileName(final String purchasevoucherNumber, final Integer purchaseVoucherLine) {
//        final TPurchaseEntity purchase = tPurchaseRepository.findByVoucherNumAndVoucherLine(purchasevoucherNumber, purchaseVoucherLine).orElseThrow(
//                () -> new ResourceNotFoundException(
//                        ResultMessages.warning().add(
//                                MessageCodeType.CODE_002, LogStringUtil.of("generatedPDFFileName")
//                                        .message("t_purchase not found.")
//                                        .value("purchase_voucher_number", purchasevoucherNumber)
//                                        .value("purchase_voucher_line", purchaseVoucherLine)
//                                        .build())));
	private String generatedPDFFileName(final String purchasevoucherNumber, final Integer purchaseVoucherLine, final BigInteger orderId) {
    	final TPurchaseEntity purchase = tPurchaseRepository.findByVoucherNumAndVoucherLineAndOrderId(purchasevoucherNumber, purchaseVoucherLine, orderId).orElseThrow(
                () -> new ResourceNotFoundException(
                        ResultMessages.warning().add(
                                MessageCodeType.CODE_002, LogStringUtil.of("generatedPDFFileName")
                                        .message("t_purchase not found.")
                                        .value("purchase_voucher_number", purchasevoucherNumber)
                                        .value("purchase_voucher_line", purchaseVoucherLine)
                                        .value("order_id", orderId)
                                        .build())));
        //PRD_0202 mod JFE end

        // ファイル名：品番CHAR(8)-種別CHAR(2)発注番号CHAR(8)種別CHAR(2).pdf
        final String partNo = purchase.getPartNo();
        final BigInteger orderNumber = purchase.getOrderNumber();
        final String fileName = String.format("%s-%s%s%s.pdf", partNo, PDF_FILENAME_TYPE_JR, orderNumber, PDF_FILENAME_TYPE_JR);

        return fileName;
    }

    /**
     * ファイル削除.
     * @param temporayFolder 一時フォルダ
     * @param xmlPath XMLファイルパス
     * @param pdfPath PDFファイルパス
     */
    private void deleteFile(final String temporayFolder, final Path xmlPath, final Path pdfPath) {
        try {
            // 一時フォルダのXMLファイルを削除
            Files.delete(xmlPath);
            // 一時フォルダのPDFファイルを削除
            Files.delete(pdfPath);
            // 一時ファイルと発注一時ディレクトリを削除
            Files.delete(Paths.get(temporayFolder));
        } catch (Exception e) {
            // ファイル削除失敗の場合は、ワーニングログを表示するが、処理は正常処理とする
            log.warn(e.getMessage());
        }
    }

    /**
     * 処理対象の返品伝票管理情報のステータスを更新.
     * @param status SendMailStatusType
     * @param voucherNumber 仕入伝票管理情報ID
     * @param userId ユーザID
     */
    private void updateStatus(final SendMailStatusType status, final BigInteger id, final BigInteger userId) {
    	tPurchasesVoucherRepository.updateStatusById(status.getValue(), id, userId);
    }

    /**
     * メール送信.
     * @param sendMailInfo 送信メール情報
     * @param files 添付ファイル情報
     * @param voucherNumbers
     * @param userId
     */
    private void sendMail(final List<TemporaryFileForPdfGenerationModel> files,TPurchasesVoucherEntity firstEntity, List<String> voucherNumbers, BigInteger userId) {
    	String supplierCode = firstEntity.getSupplierCode();
        final String bccMaileAddress = mailAddressComponent.getPurchaseItemMailaddress(Optional.ofNullable(supplierCode).orElse(StringUtils.EMPTY));

        // PDFファイルのリストを抽出
        final List<MailAttachementFileModel> listFile = files.stream().map(file -> {
            final MailAttachementFileModel mailAttachementFileModel = new MailAttachementFileModel();
            mailAttachementFileModel.setFile(file.getPdfFilePath().toFile());
            mailAttachementFileModel.setFileName(file.getFileName());
            return mailAttachementFileModel;
        }).collect(Collectors.toList());

        //メール送信用の情報用意
        final Optional<MMailTemplateEntity> optional = mMailTemplateRepository.findByMailCode(MMailCodeType.PURCHASE_ITEM_SEND_MAIL);
        final MMailTemplateEntity mMailTemplateEntity = optional.get();
        final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();
        if (!optional.isPresent()) {
            log.error("メールのテンプレートが取得できませんでした。メールコード：" + MMailCodeType.PURCHASE_ITEM_SEND_MAIL);
            return;
        }

      //PRD_0187 JFE mod start
//        final SendMailType sendMailType = mailSenderAttachmentComponent.sendMail(
//        		commonPropertyModel.getSendMailFrom(),
//        		commonPropertyModel.getSendMailTo(),
//        		commonPropertyModel.getSendMailCc(),
//                bccMaileAddress.trim(),
//                mMailTemplateEntity.getTitle(),
//                mMailTemplateEntity.getContent(),
//                listFile);
        final SendMailType sendMailType = mailSenderAttachmentComponent.sendPurchaseVoucherMail(
        		commonPropertyModel.getSendMailFrom(),
        		commonPropertyModel.getSendMailTo(),
        		commonPropertyModel.getSendMailCc(),
                bccMaileAddress.trim(),
                mMailTemplateEntity.getTitle(),
                mMailTemplateEntity.getContent(),
                commonPropertyModel.getSendMailSignature(),
                listFile);

      //PRD_0187 JFE mod end
        //メールの内容をログ出力
        //ログ用ファイル名取得
        List<String> logFileNames = listFile.stream().map(file -> file.getFileName()).collect(Collectors.toList());

        if (sendMailType == SendMailType.MAIL_UNSENT || sendMailType == SendMailType.MAIL_SENDING_ERROR) {
            // メール送信されていない場合エラーとする
            throw new ScheduleException(LogStringUtil.of("sendMail")
                    .message("The email was not sent.")
                    .value("from_mail_address", commonPropertyModel.getSendMailFrom())
                    .value("to_mail_address", commonPropertyModel.getSendMailTo())
                    .value("cc_mail_address", commonPropertyModel.getSendMailCc())
                    .value("bcc_mail_address", bccMaileAddress.trim())
        			  .value("添付ファイル名",logFileNames)
                    .build());

        }


        log.info(LogStringUtil.of("sendMails_PurchaseItem")
                .message("メールを送信しました.")
                .value("送信元メールアドレス", commonPropertyModel.getSendMailFrom())
                .value("送信先メールアドレス", commonPropertyModel.getSendMailTo())
                .value("Ccメールアドレス", commonPropertyModel.getSendMailCc())
                .value("Bccメールアドレス", bccMaileAddress.trim())
                .value("件名",mMailTemplateEntity.getTitle())
                .value("本文",mMailTemplateEntity.getContent())
                .value("添付ファイル名",logFileNames)
                .build());

        //仕入ファイル情報更新(状態：送信済み)
        voucherNumbers.stream().forEach(voucherNumber -> {
        tPurchaseFileInfoRepository.updateStatusByVoucherNumber(voucherNumber,userId);
        });
    }
}
//PRD_0134 #10654 add JEF end
