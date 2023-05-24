package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcSirmstModel;
import jp.co.jun.edi.model.JunpcSirmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.JunpcSirmstListService;
import jp.co.jun.edi.service.parameter.ListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 発注生産システムの仕入先マスタを取得するAPI.
 */
@RestController
@RequestMapping("/api/v1/junpc/sirmst")
@Slf4j
public class JunpcSirmstV1Api {
    @Autowired
    private JunpcSirmstListService listService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 発注生産システムの仕入先マスタをリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link JunpcSirmstSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    @GetMapping
    public GenericListMobel<JunpcSirmstModel> list(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @ModelAttribute @Validated final JunpcSirmstSearchConditionModel searchCondition) {
        final JunpcSirmstSearchConditionModel localSearchCondition = getSearchCondition(searchCondition);

        final ListServiceResponse<JunpcSirmstModel> serviceResponse = listService
                .call(ListServiceParameter.<JunpcSirmstSearchConditionModel>builder().loginUser(loginUser).searchCondition(localSearchCondition).build());

        final GenericListMobel<JunpcSirmstModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * モデルを取得します.
     *
     * @param searchCondition {@link JunpcSirmstSearchConditionModel} instance
     * @return {@link JunpcSirmstSearchConditionModel} instance
     */
    private JunpcSirmstSearchConditionModel getSearchCondition(
            final JunpcSirmstSearchConditionModel searchCondition) {
        if (StringUtils.isNotEmpty(searchCondition.getPageToken())) {
            try {
                final JunpcSirmstSearchConditionModel localSearchCondition = objectMapper.readValue(
                        Base64.getDecoder().decode(searchCondition.getPageToken()),
                        JunpcSirmstSearchConditionModel.class);

                log.info(localSearchCondition.toString());

                return localSearchCondition;
            } catch (IOException e) {
                log.warn("IOException.", e);
            }
        }

        return searchCondition;
    }

    /**
     * NextPageTokenを取得します.
     *
     * @param searchCondition {@link JunpcSirmstSearchConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final JunpcSirmstSearchConditionModel searchCondition,
            final boolean nextPage) {
        if (nextPage) {
            searchCondition.setPageToken(null);
            searchCondition.setPage(searchCondition.getPage() + 1);

            try {
                return Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(searchCondition));
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException.", e);
            }
        }

        return "";
    }
}
