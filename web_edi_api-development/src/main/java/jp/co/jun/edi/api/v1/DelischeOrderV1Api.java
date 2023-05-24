package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.model.DelischeOrderSearchConditionModel;
import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.VDelischeOrderModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.DelischeOrderListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * デリスケ発注API.
 */
@RestController
@RequestMapping("/api/v1/delischeOrders")
@Secured("ROLE_JUN")
@Slf4j
public class DelischeOrderV1Api {
    @Autowired
    private DelischeOrderListService listService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * デリスケ発注情報をリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link DelischeOrderSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @PostMapping
    public GenericListMobel<VDelischeOrderModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestBody final DelischeOrderSearchConditionModel searchCondition) {

        final DelischeOrderSearchConditionModel localSearchCondition = getSearchCondition(searchCondition);

        final ListServiceResponse<VDelischeOrderModel> serviceResponse = listService
                .call(ListServiceParameter.<DelischeOrderSearchConditionModel>builder().loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<VDelischeOrderModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * デリスケ発注情報検索用のモデルを取得します.
     *
     * @param searchCondition {@link DelischeOrderSearchConditionModel} instance
     * @return {@link DelischeOrderSearchConditionModel} instance
     */
    private DelischeOrderSearchConditionModel getSearchCondition(
            final DelischeOrderSearchConditionModel searchCondition) {
        if (StringUtils.isEmpty(searchCondition.getPageToken())) {
            return searchCondition;
        }

        try {
            final DelischeOrderSearchConditionModel localSearchCondition = objectMapper.readValue(
                    Base64.getDecoder().decode(searchCondition.getPageToken()),
                    DelischeOrderSearchConditionModel.class);

            log.info(localSearchCondition.toString());

            return localSearchCondition;
        } catch (IOException e) {
            log.warn("IOException.", e);
        }
        return searchCondition;
    }

    /**
     * NextPageTokenを取得します.
     *
     * @param searchCondition {@link DelischeOrderSearchConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final DelischeOrderSearchConditionModel searchCondition,
            final boolean nextPage) {

        String nextPageToken = "";

        if (!nextPage) {
            return nextPageToken;
        }

        searchCondition.setPageToken(null);
        searchCondition.setPage(searchCondition.getPage() + 1);

        try {
            nextPageToken = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(searchCondition));
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException.", e);
        }

        return nextPageToken;
    }
}
