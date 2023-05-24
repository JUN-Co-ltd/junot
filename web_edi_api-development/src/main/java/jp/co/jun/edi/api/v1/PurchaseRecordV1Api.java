//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.api.v1;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.validation.groups.Default;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.PurchaseRecordCreatePdfComponent;
import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.FileModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.PurchaseRecordCsvModel;
import jp.co.jun.edi.model.PurchaseRecordSearchConditionModel;
import jp.co.jun.edi.model.PurchaseRecordSearchResultModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.PurchaseRecordCsvService;
import jp.co.jun.edi.service.PurchaseRecordSearchService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
//import jp.co.jun.edi.util.DataBaseSelectHolder;

/**
 * 仕入API.
 */
@RestController
@RequestMapping("/api/v1/purchasesRecord")
public class PurchaseRecordV1Api {

	@Autowired
	private PurchaseRecordSearchService searchService;

	@Autowired
	private SearchConditionComponent searchConditionComponent;

	@Autowired
	private PurchaseRecordCsvService csvSearchService;

	@Autowired
	private PurchaseRecordCreatePdfComponent pdfCreateComponent;

	/**
	 * 仕入実績一覧を検索する.
	 *
	 * @param loginUser {@link CustomLoginUser} instance
	 * @param searchCondition {@link PurchaseRecordSearchConditionModel} instance
	 * @return {@link GenericListModel} instance
	 */
	@PostMapping("/search")
	// PRD_0166 #10181 jfe del start
	//@PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
	// PRD_0166 #10181 jfe del end
	public GenericListMobel<PurchaseRecordSearchResultModel> search(
			@AuthenticationPrincipal final CustomLoginUser loginUser,
			@RequestBody @Validated(Default.class) final PurchaseRecordSearchConditionModel searchCondition) {
		final PurchaseRecordSearchConditionModel localSearchCondition = searchConditionComponent
				.getSearchCondition(searchCondition, PurchaseRecordSearchConditionModel.class);

		// 20230419 jfe del start
		//DataBaseSelectHolder.setDataBaseInstanceInfo("replica");
		// 20230419 jfe del end


		final ListServiceResponse<PurchaseRecordSearchResultModel> serviceResponse = searchService
				.call(ListServiceParameter.<PurchaseRecordSearchConditionModel> builder()
						.loginUser(loginUser).searchCondition(localSearchCondition).build());

		// 20230419 jfe del start
		//DataBaseSelectHolder.setDataBaseInstanceInfo("primary");
		// 20230419 jfe del end

		final GenericListMobel<PurchaseRecordSearchResultModel> response = new GenericListMobel<>();
		response.setItems(serviceResponse.getItems());
		response.setNextPageToken(searchConditionComponent.getNextPageToken(
				localSearchCondition,
				serviceResponse.isNextPage()));
		return response;
	}

	/**
	 * 仕入実績CSV情報検索する.
	 * @param loginUser {@link CustomLoginUser} instance
	 * @param searchCondition {@link PurchaseRecordSearchConditionModel} instance
	 * @return {@link GenericListModel} instance
	 */
	@PostMapping("/search-csv")
    // PRD_0166 #10181 jfe del start
    //@PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
    // PRD_0166 #10181 jfe del end
	public GenericListMobel<PurchaseRecordCsvModel> searchCsv(
			@AuthenticationPrincipal final CustomLoginUser loginUser,
			@RequestBody @Validated(Default.class) final PurchaseRecordSearchConditionModel searchCondition) {
		final PurchaseRecordSearchConditionModel localSearchCondition = searchConditionComponent
				.getSearchCondition(searchCondition, PurchaseRecordSearchConditionModel.class);

		// 20230419 jfe del start
		//DataBaseSelectHolder.setDataBaseInstanceInfo("replica");
		// 20230419 jfe del end

		//ここのサービスを画面上の検索条件で全件検索するサービスに変更をする。
		final ListServiceResponse<PurchaseRecordCsvModel> serviceResponse = csvSearchService
				.call(ListServiceParameter.<PurchaseRecordSearchConditionModel> builder()
						.loginUser(loginUser).searchCondition(localSearchCondition).build());

		// 20230419 jfe del start
		//DataBaseSelectHolder.setDataBaseInstanceInfo("primary");
		// 20230419 jfe del end

		final GenericListMobel<PurchaseRecordCsvModel> response = new GenericListMobel<>();
		response.setItems(serviceResponse.getItems());
		response.setNextPageToken(searchConditionComponent.getNextPageToken(
				localSearchCondition,
				serviceResponse.isNextPage()));
		return response;
	}

	/**
	 * 仕入実績PDF作成.
	 * @param loginUser {@link CustomLoginUser} instance
	 * @param searchCondition {@link PurchaseRecordSearchConditionModel} instance
	 * @return {@link GenericListModel} instance
	 */
	@GetMapping("/create-pdf")
    // PRD_0166 #10181 jfe del start
    //@PreAuthorize("hasRole('ROLE_DISTA') or hasRole('ROLE_ADMIN')")
    // PRD_0166 #10181 jfe del end
	public HttpEntity<byte[]> createPdf(
			@AuthenticationPrincipal final CustomLoginUser loginUser,
			@ModelAttribute final PurchaseRecordSearchConditionModel searchCondition) {
		final PurchaseRecordSearchConditionModel localSearchCondition = searchConditionComponent
				.getSearchCondition(searchCondition, PurchaseRecordSearchConditionModel.class);

		final FileModel file = pdfCreateComponent.createPDF(localSearchCondition);

		String encFileName;
		try {
			encFileName = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 not supported");
		}

		final MediaType mediaType = MediaType.valueOf(file.getContentType());
		final byte[] fileByteArray = file.getFileData();
		ContentDisposition contentDisposition = ContentDisposition.parse("attachment; filename=" + encFileName);
		return new HttpEntity<byte[]>(fileByteArray,
				toHttpHeaders(mediaType, fileByteArray.length, contentDisposition));
	}

	/**
	 * @param mediaType  {@link MediaType} instance
	 * @param contentLength  contentLength
	 * @param contentDisposition  {@link ContentDisposition} instance
	 * @return {@link HttpHeaders} instance
	 */
	private HttpHeaders toHttpHeaders(
			final MediaType mediaType,
			final long contentLength,
			final ContentDisposition contentDisposition) {
		final HttpHeaders headers = new HttpHeaders();

		headers.setContentType(mediaType);
		headers.setContentLength(contentLength);
		headers.setContentDisposition(contentDisposition);
		List<String> exposedHeaders = new ArrayList<String>();
		exposedHeaders.add("Content-Disposition");
		headers.setAccessControlExposeHeaders(exposedHeaders);

		return headers;
	}

}
//PRD_0133 #10181 add JFE end