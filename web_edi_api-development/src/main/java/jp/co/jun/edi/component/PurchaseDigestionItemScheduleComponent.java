package jp.co.jun.edi.component;

import java.math.BigDecimal;
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

import jp.co.jun.edi.component.mail.MailAddressComponent;
import jp.co.jun.edi.component.mail.MailSenderAttachmentComponent;
import jp.co.jun.edi.component.model.CommonPropertyModel;
import jp.co.jun.edi.component.model.MailAttachementFileModel;
import jp.co.jun.edi.component.model.TemporaryFileForPdfGenerationModel;
import jp.co.jun.edi.entity.MMailTemplateEntity;
import jp.co.jun.edi.entity.TPurchaseDigestionItemPDFEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.repository.MMailTemplateRepository;
import jp.co.jun.edi.type.MMailCodeType;
import jp.co.jun.edi.type.SendMailType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

//PRD_0134 #10654 add JEF start
@Slf4j
@Component
public class PurchaseDigestionItemScheduleComponent {
	/** PDFファイル.ファイル名.種別(JR：仕入明細). */
	private static final String PDF_FILENAME_TYPE_JR = "JR";
	@Autowired
	private PropertyComponent propertyComponent;
	@Autowired
	private PurchaseDigestionItemCreateXmlComponent createXmlComponent;
	@Autowired
	private PurchaseDigestionItemCreatePdfComponent createPdfComponent;
	@Autowired
	private MailAddressComponent mailAddressComponent;
	@Autowired
	private MMailTemplateRepository mMailTemplateRepository;
	@Autowired
	private MailSenderAttachmentComponent mailSenderAttachmentComponent;

	/**
	*
	* 仕入伝票作成処理の実行.
	* @param list 仕入伝票管理情報
	 * @param yyyyMM
	 * @param yyMMdd
	*/
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void execute(final List<TPurchaseDigestionItemPDFEntity> list, String yyyyMM, String yyMMdd) {
		log.info(LogStringUtil.of("execute")
				.message("Start processing of PurchaseDigestionItemSchedule.")
				.value("PartNo", list.get(0).getPartNo())
				.value("SupplierCode", list.get(0).getSupplierCode())
				.build());

		List<TPurchaseDigestionItemPDFEntity> CreatePDFEntity = new ArrayList<TPurchaseDigestionItemPDFEntity>();

		// 一時フォルダに生成されたXMLファイル、PDFファイル格納用
		final List<TemporaryFileForPdfGenerationModel> files = new ArrayList<>();

		//最初に取ってきたリストを１件ずつ確認して、集約条件が同じ場合はリストに追加（そのリスト単位でPDF作成する)
		//リストの先頭は比較しない。（判断用のフラグ
		boolean firstPDFEntity = true;
		//リストの最後の場合は強制でPDF作成する。
		boolean lastPDFEntity = false;
		// PRD_0209 && TEAM_ALBUS-41 add start リストの最終行が集約条件関係なくセットしてしまっている不具合対策
		list.add(new TPurchaseDigestionItemPDFEntity());
		// PRD_0209 && TEAM_ALBUS-41 add end

		//仕入先(比較元
		String FsupplierCode = "";
		//製品工場(比較元
		String FmdfMakerFactory = "";
		//入荷場所(比較元
		String FarrivalPlace = "";
		//伝票区分(比較元
		String Fdenk = "";
		//品番(比較元
		String FpartNo = "";
		//上代(比較元
		BigDecimal FretailPrice = null;
		//単価(比較元
		BigDecimal FunitPrice = null;

		for (int i = 0; i < list.size(); i++) {
			try {
				//集約条件の項目を抜き出して比較
				final TPurchaseDigestionItemPDFEntity readingPDFEntity = list.get(i);
				//仕入先(比較先
				String supplierCode = readingPDFEntity.getSupplierCode();
				//製品工場(比較先 NULLの可能性あるからその場合は空文字にする
				String mdfMakerFactory = "";
				if (readingPDFEntity.getMdfMakerFactoryCode() == null) {
				} else {
					mdfMakerFactory = readingPDFEntity.getMdfMakerFactoryCode();
				}
				//入荷場所(比較先
				String arrivalPlace = readingPDFEntity.getArrivalPlace();
				//伝票区分(比較先
				String denk = readingPDFEntity.getPurchaseVoucherType();
				//品番(比較先
				String partNo = readingPDFEntity.getPartNo();
				//上代(比較先
				BigDecimal retailPrice = readingPDFEntity.getRetailPrice();
				//単価(比較先
				BigDecimal unitPrice = readingPDFEntity.getUnitPrice();

				//ここで比較処理。　初回、もしくは集約条件が同じだった場合は、出力情報リストに入れて次へ…
				//PRD_0177 mod JFE start
//				if (((firstPDFEntity == true) || ((FsupplierCode.equals(supplierCode))
//						&& (FmdfMakerFactory.equals(mdfMakerFactory)) && (FarrivalPlace.equals(arrivalPlace))
//						&& (Fdenk.equals(denk))
//						&& (FpartNo.equals(partNo)) && (FretailPrice.equals(retailPrice))
//						&& (FunitPrice.equals(unitPrice)))) && (lastPDFEntity == false)) {
                if ((((firstPDFEntity == true) || ((FsupplierCode.equals(supplierCode))
						&& (FmdfMakerFactory.equals(mdfMakerFactory)) && (FarrivalPlace.equals(arrivalPlace))
						&& (Fdenk.equals(denk))
						&& (FpartNo.equals(partNo)) && (FretailPrice.equals(retailPrice))
                        && (FunitPrice.equals(unitPrice)))) && (lastPDFEntity == false)) && (list.size() != 1)) {
				//PRD_0177 mod JFE end
					CreatePDFEntity.add(readingPDFEntity);
					firstPDFEntity = false;
					FsupplierCode = supplierCode;
					FmdfMakerFactory = mdfMakerFactory;
					FarrivalPlace = arrivalPlace;
					Fdenk = denk;
					FpartNo = partNo;
					FretailPrice = retailPrice;
					FunitPrice = unitPrice;
				} else {
					//集約項目の内容が1か所でも変わったら、それまでの情報をまとめてPDF作成+メール送信
					final TemporaryFileForPdfGenerationModel file = new TemporaryFileForPdfGenerationModel();
					// PRD_0209 && TEAM_ALBUS-41 add start リストの最終行が集約条件関係なくセットしてしまっている不具合対策
//					//リストの最後だった場合は、出力するリストに含める。でないと出力されない
//					//PRD_0177 mod JFE start
////					if (lastPDFEntity == true) {
//	                if ((lastPDFEntity == true) || (list.size()==1)) {
//					//PRD_0177 mod JFE end
//						CreatePDFEntity.add(readingPDFEntity);
//					}
	                // PRD_0209 && TEAM_ALBUS-41 add end
					// プロパティ情報取得
					// 一時フォルダ
					final String temporayFolder = getTemporaryPath(CreatePDFEntity.get(0).getPartNo(), yyyyMM);
					// XMLファイルパス
					final Path xmlPath = generatedXmlFilePath(temporayFolder, CreatePDFEntity.get(0).getPartNo());
					// PDFファイルパス
					final Path pdfPath = generatedPdfFilePath(temporayFolder, CreatePDFEntity.get(0).getPartNo());
					//PRD_0177 mod JFE start
					// XSLファイルパス
//					final Path xslPath = getXslPath();
					final Path xslPath = getXslPath(CreatePDFEntity.get(0));
					//PRD_0177 mod JFE end
					// PDFファイル名
					final String fileName = generatedPDFFileName(CreatePDFEntity.get(0).getPartNo());
					// XML作成
					createXmlComponent.createXml(CreatePDFEntity, xmlPath, yyMMdd);
					// PDF作成
					createPdfComponent.createPdf(xslPath, xmlPath, pdfPath, fileName);

					//PRD_0152 del JFE start
					// 一時フォルダのXMLファイル、PDFファイルを削除
					//deleteFile(temporayFolder, xmlPath, pdfPath);
					//PRD_0152 del JFE end

					//PDFの作成が出来たらメール送信
					//ファイル情報に必要な項目をセット
					// 一時フォルダ
					file.setTemporayFolder(temporayFolder);
					// XMLファイルパス
					file.setXmlFilePath(xmlPath);
					// PDFファイルパス
					file.setPdfFilePath(pdfPath);
					// PDFファイル名
					file.setFileName(fileName);

					//一旦初期化
					files.clear();
					files.add(file);
					if (!files.isEmpty()) {
						//メール送信
						//PRD_0152 mod JFE start
//						TPurchaseDigestionItemPDFEntity sendPDFEntity = new TPurchaseDigestionItemPDFEntity();
//						sendPDFEntity = CreatePDFEntity.get(0);
//						sendMail(files, sendPDFEntity);
						TPurchaseDigestionItemPDFEntity sendPDFEntity = CreatePDFEntity.get(0);
						sendMail(files, sendPDFEntity);
						//PRD_0152 mod  JFE end
					}

					//次のPDF作成に向けて準備
					FsupplierCode = supplierCode;
					if (mdfMakerFactory == null) {
						FmdfMakerFactory = "";
					} else {
						FmdfMakerFactory = mdfMakerFactory;
					}

					FarrivalPlace = arrivalPlace;
					Fdenk = denk;
					FpartNo = partNo;
					FretailPrice = retailPrice;
					FunitPrice = unitPrice;
					//リストにも追加するよ
					//のまえに一旦初期化
					CreatePDFEntity = new ArrayList<TPurchaseDigestionItemPDFEntity>();
					CreatePDFEntity.add(readingPDFEntity);
					//PRD_0152 add JFE start
					// 一時フォルダのXMLファイル、PDFファイルを削除
					deleteFile(temporayFolder, xmlPath, pdfPath);
					//PRD_0152 add JFE end
				}
				//次読込む行が最終行の場合は必ずPDF出力する。
				if (i + 2 == list.size()) {
					lastPDFEntity = true;
				}

			} catch (ResourceNotFoundException e) {
				log.warn(e.getMessage(), e);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

		}
		;
		log.info(LogStringUtil.of("execute")
				.message("End processing of PurchaseDigestionItemSchedule.")
				.value("PartNo", list.get(0).getPartNo())
				.value("SupplierCode", list.get(0).getSupplierCode())
				.build());
	}

	/**
	* 一時ディレクトリを取得する.
	* ディレクトリ構成：{一時ディレクトリ}/returnItem/{ユーザID}/
	 * @param yyyyMM
	* @param userId ユーザID
	* @return 発注別の一時ディレクトリ
	* @throws Exception 例外
	*/
	private String getTemporaryPath(final String partNo, String yyyyMM) throws Exception {
		String tmpDirectory = propertyComponent.getBatchProperty().getScheduleFopTemporaryFolder();
		if (!tmpDirectory.endsWith("/")) {
			// 接尾辞がスラッシュでない場合、接尾辞にスラッシュをつける
			tmpDirectory = tmpDirectory.concat("/");
		}
		tmpDirectory = tmpDirectory.concat("purchaseDigestionItem/").concat(yyyyMM).concat("/").concat(partNo)
				.concat("/");
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
	private Path generatedXmlFilePath(final String path, final String partNo) {
		// XMLファイル purchase_item_{伝票番号}_{発注ID}_{yyyyMMddHHmmssSSS}.xml
		final String nowDate = DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssS");
		final String xmlFile = "purchase_digestion_item_" + partNo + "_" + nowDate
				+ ".xml";
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
	private Path generatedPdfFilePath(final String path, final String purchasevoucherNumber) {
		// PDFファイル purchase_item_{伝票番号}_{発注ID}_{yyyyMMddHHmmssSSS}.xml
		final String nowDate = DateUtils.formatFromDate(DateUtils.createNow(), "yyyyMMddHHmmssS");
		final String pdfFile = "purchase_digestion_item_" + purchasevoucherNumber + "_" + nowDate
				+ ".pdf";

		return Paths.get(path + pdfFile);
	}

	/**
	* XSLファイルパスを取得.
	* @return XSLファイルパス
	* @throws Exception 例外
	*/
	//PRD_0177 mod JFE start
//	private Path getXslPath() throws Exception {
//		//PRD_0151 mod JFE start
//		//		final String strXslPath = "C:\\Users\\KIT76807\\Desktop\\JUN\\web_edi_api-staging\\config\\pdf\\xsl\\purchase-item.xsl";
//		final String strXslPath = propertyComponent.getBatchProperty().getPurchaseItemPathXsl();
//		//PRD_0151 mod JFE end
	 private Path getXslPath(TPurchaseDigestionItemPDFEntity tPurchaseDigestionItemPDFEntity) throws Exception {
	     final String denk = tPurchaseDigestionItemPDFEntity.getPurchaseVoucherType().substring(0,1);
	     final String arrivalPlace = tPurchaseDigestionItemPDFEntity.getArrivalPlace();
	     String strXslPath = "";
	     if ((denk.equals("4")|| denk.equals("6")) && arrivalPlace.equals("19")) {//返品の場合は返品用のレイアウトを使用
	         strXslPath = propertyComponent.getBatchProperty().getPurchaseDigestionReturnPathXsl();
	     }else {
	         strXslPath = propertyComponent.getBatchProperty().getPurchaseItemPathXsl();
	     }
	 //PRD_0177 mod JFE end
		final Path xslPath = Paths.get(strXslPath);
		if (!Files.exists(xslPath)) {
			throw new ScheduleException(LogStringUtil.of("getXslPath")
					.message("not find path.")
					.value("purchase-item.path.xsl", strXslPath)
					.build());
		}
		return xslPath;
	}

	/**
	* 仕入明細PDFファイル名 生成.
	*
	* 品番CHAR(8)-種別CHAR(2)
	* 例）ANM59050-JR.pdf
	*
	* @param purchasevoucherNumber 伝票番号
	* @param purchaseVoucherLine 仕入伝票行
	* @return PDF名
	*/
	private String generatedPDFFileName(final String PartNo) {

		// ファイル名：品番CHAR(8)-種別CHAR(2).pdf
		final String fileName = String.format("%s-%s.pdf", PartNo, PDF_FILENAME_TYPE_JR);

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
			//PRD_0152 mod JFE start
			log.warn(e.getMessage());
			//PRD_0152 mod JFE end
		}
	}

	/**
	* メール送信.
	* @param sendMailInfo 送信メール情報
	* @param files 添付ファイル情報
	* @param voucherNumbers
	* @param userId
	*/
	private void sendMail(final List<TemporaryFileForPdfGenerationModel> files,
			TPurchaseDigestionItemPDFEntity firstEntity) {
		String supplierCode = firstEntity.getSupplierCode();
		String partNo = firstEntity.getPartNo();
		final String bccMaileAddress = mailAddressComponent
				.getPurchaseItemMailaddress(Optional.ofNullable(supplierCode).orElse(StringUtils.EMPTY));

		// PDFファイルのリストを抽出
		final List<MailAttachementFileModel> listFile = files.stream().map(file -> {
			final MailAttachementFileModel mailAttachementFileModel = new MailAttachementFileModel();
			mailAttachementFileModel.setFile(file.getPdfFilePath().toFile());
			mailAttachementFileModel.setFileName(file.getFileName());
			return mailAttachementFileModel;
		}).collect(Collectors.toList());

		//メール送信用の情報用意
		final Optional<MMailTemplateEntity> optional = mMailTemplateRepository
				.findByMailCode(MMailCodeType.PURCHASE_ITEM_SEND_MAIL);
		final MMailTemplateEntity mMailTemplateEntity = optional.get();
		final CommonPropertyModel commonPropertyModel = propertyComponent.getCommonProperty();
		if (!optional.isPresent()) {
			log.error("メールのテンプレートが取得できませんでした。メールコード：" + MMailCodeType.PURCHASE_ITEM_SEND_MAIL);
			return;
		}

		//メールが送られないようにコメントアウト　ここから
		//PRD_0188 JFE mod start
//		final SendMailType sendMailType = mailSenderAttachmentComponent.sendMail(
//				commonPropertyModel.getSendMailFrom(),
//				commonPropertyModel.getSendMailTo(),
//				commonPropertyModel.getSendMailCc(),
//				bccMaileAddress.trim(),
//				mMailTemplateEntity.getTitle(),
//				mMailTemplateEntity.getContent(),
//				listFile);
        final SendMailType sendMailType = mailSenderAttachmentComponent.sendPurchaseVoucherMail(
				commonPropertyModel.getSendMailFrom(),
				commonPropertyModel.getSendMailTo(),
				commonPropertyModel.getSendMailCc(),
				bccMaileAddress.trim(),
				mMailTemplateEntity.getTitle(),
				mMailTemplateEntity.getContent(),
                commonPropertyModel.getSendMailSignature(),
				listFile);
		//PRD_0188 JFE mod end
		//メールが送られないようにコメントアウト ここまで

		//メールの内容をログ出力
		//ログ用ファイル名取得
		List<String> logFileNames = listFile.stream().map(file -> file.getFileName()).collect(Collectors.toList());

		//PRD_0152 mod JFE start
//		log.info(LogStringUtil.of("sendMails_PurchaseItem")
//				.message("メールを送信しました.")
//				.value("送信元メールアドレス", commonPropertyModel.getSendMailFrom())
//				.value("送信先メールアドレス", commonPropertyModel.getSendMailTo())
//				.value("Ccメールアドレス", commonPropertyModel.getSendMailCc())
//				.value("Bccメールアドレス", bccMaileAddress.trim())
//				.value("件名", mMailTemplateEntity.getTitle())
//				.value("本文", mMailTemplateEntity.getContent())
//				.value("品番", partNo)
//				.value("仕入先コード", supplierCode)
//				.value("添付ファイル名", logFileNames)
//				.build());
				if (sendMailType == SendMailType.MAIL_UNSENT || sendMailType == SendMailType.MAIL_SENDING_ERROR) {
					// メール送信されていない場合エラーとする
					throw new ScheduleException(LogStringUtil.of("sendMail")
							.message("The email was not sent.")
							.value("送信元メールアドレス", commonPropertyModel.getSendMailFrom())
							.value("送信先メールアドレス", commonPropertyModel.getSendMailTo())
							.value("Ccメールアドレス", commonPropertyModel.getSendMailCc())
							.value("Bccメールアドレス", bccMaileAddress.trim())
							.value("件名", mMailTemplateEntity.getTitle())
							.value("本文", mMailTemplateEntity.getContent())
							.value("品番", partNo)
							.value("仕入先コード", supplierCode)
							.value("添付ファイル名", logFileNames)
							.build());
				} else {
					log.info(LogStringUtil.of("sendMails_PurchaseItem")
							.message("メールを送信しました.")
							.value("送信元メールアドレス", commonPropertyModel.getSendMailFrom())
							.value("送信先メールアドレス", commonPropertyModel.getSendMailTo())
							.value("Ccメールアドレス", commonPropertyModel.getSendMailCc())
							.value("Bccメールアドレス", bccMaileAddress.trim())
							.value("件名", mMailTemplateEntity.getTitle())
							.value("本文", mMailTemplateEntity.getContent())
							.value("品番", partNo)
							.value("仕入先コード", supplierCode)
							.value("添付ファイル名", logFileNames)
							.build());
				}
		//PRD_0152 mod JFE end
	}

}
//PRD_0134 #10654 add JEF end