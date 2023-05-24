package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.model.GenericListMobel;
import jp.co.jun.edi.model.JunpcCodmstModel;
import jp.co.jun.edi.model.JunpcCodmstSearchConditionModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericMCodmstListService;
import jp.co.jun.edi.service.parameter.JunpcCodmstListServiceParameter;
import jp.co.jun.edi.service.response.ListServiceResponse;
import jp.co.jun.edi.type.MCodmstTblIdType;
import lombok.extern.slf4j.Slf4j;

/**
 * 発注生産システムのコードマスタからリストを取得するAPI.
 */
@Slf4j
public abstract class GenericJunpcCodmstV1Api {
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 発注生産システムのコードマスタをリストで取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param searchCondition {@link JunpcCodmstSearchConditionModel} instance
     * @return {@link GenericListMobel} instance
     */
    protected GenericListMobel<JunpcCodmstModel> search(
            final CustomLoginUser loginUser,
            final JunpcCodmstSearchConditionModel searchCondition) {
        final JunpcCodmstSearchConditionModel localSearchCondition = getSearchCondition(searchCondition);

        final ListServiceResponse<JunpcCodmstModel> serviceResponse = getListService()
                .call(JunpcCodmstListServiceParameter.builder().loginUser(loginUser).tblId(getTblId()).searchCondition(localSearchCondition).build());

        final GenericListMobel<JunpcCodmstModel> response = new GenericListMobel<>();
        response.setItems(serviceResponse.getItems());
        response.setNextPageToken(getNextPageToken(
                localSearchCondition,
                serviceResponse.isNextPage()));

        return response;
    }

    /**
     * モデルを取得します.
     *
     * @param searchCondition {@link JunpcCodmstSearchConditionModel} instance
     * @return {@link JunpcCodmstSearchConditionModel} instance
     */
    private JunpcCodmstSearchConditionModel getSearchCondition(
            final JunpcCodmstSearchConditionModel searchCondition) {
        if (StringUtils.isNotEmpty(searchCondition.getPageToken())) {
            try {
                final JunpcCodmstSearchConditionModel localSearchCondition = objectMapper.readValue(
                        Base64.getDecoder().decode(searchCondition.getPageToken()),
                        JunpcCodmstSearchConditionModel.class);

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
     * @param searchCondition {@link JunpcCodmstSearchConditionModel} instance
     * @param nextPage 次のページ
     * @return NextPageToken
     */
    private String getNextPageToken(
            final JunpcCodmstSearchConditionModel searchCondition,
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

    /**
     * テーブルIDを取得する.
     *
     * @return テーブルID
     */
    protected abstract MCodmstTblIdType getTblId();

    /**
     * 検索用のサービスを取得する.
     *
     * @return 検索用のサービス
     */
    protected abstract GenericMCodmstListService getListService();
}
