package jp.co.jun.edi.api.v1;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.co.jun.edi.component.SearchConditionComponent;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.NewsModel;
import jp.co.jun.edi.model.NewsSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.NewsGetService;
import jp.co.jun.edi.service.NewsListService;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;

/**
 * お知らせ情報API.
 */
@RestController
@RequestMapping("/api/v1/news")
public class NewsV1Api {

    @Autowired
    private NewsGetService getService;

    @Autowired
    private NewsListService listService;

    @Autowired
    private SearchConditionComponent searchConditionComponent;

    /**
     * お知らせ情報を取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param newsId お知らせID
     * @return {@link NewsModel} instance
     */
    @GetMapping("/{newsId}")
    public NewsModel get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("newsId") final BigInteger newsId) {
        return getService.call(GetServiceParameter.<BigInteger>builder().loginUser(loginUser).id(newsId).build()).getItem();
    }

    /**
     * お知らせ情報をリストで取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link NewsSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping("/search")
    public GenericListMobel<NewsModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody @Validated final NewsSearchConditionModel searchCondition) {
        final NewsSearchConditionModel localSearchCondition = searchConditionComponent
                .getSearchCondition(searchCondition, NewsSearchConditionModel.class);

        final ListServiceResponse<NewsModel> serviceResponse = listService
                .call(ListServiceParameter.<NewsSearchConditionModel>builder().loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<NewsModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(searchConditionComponent.getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }
}
