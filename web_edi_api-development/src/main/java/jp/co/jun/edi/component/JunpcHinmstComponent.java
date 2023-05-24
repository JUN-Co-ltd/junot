package jp.co.jun.edi.component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.json.Json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.jun.edi.component.model.JunpcHinmstExistsByPartNoResultModel;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.type.MessageCodeType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 発注生産システムの品番マスタ関連のコンポーネント.
 */
@Component
@Slf4j
public class JunpcHinmstComponent extends GenericComponent {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".component.junpc-hinmst-component";

    /** URL. */
    @Value("${" + PROPERTY_NAME_PREFIX + ".url}")
    private String url;

    /** HTTP Connect Timeout (MILLISECONDS). */
    @Value("${" + PROPERTY_NAME_PREFIX + ".http-connect-timeout}")
    private long connectTimeout;

    /** HTTP Read Timeout (MILLISECONDS). */
    @Value("${" + PROPERTY_NAME_PREFIX + ".http-read-timeout}")
    private long readTimeout;

    /** HTTP Write Timeout (MILLISECONDS). */
    @Value("${" + PROPERTY_NAME_PREFIX + ".http-write-timeout}")
    private long writeTimeout;

    /** HTTP Log Level. */
    @Value("${" + PROPERTY_NAME_PREFIX + ".http-log-level}")
    private HttpLoggingInterceptor.Level logLevel;

    @Autowired
    private ObjectMapper objectMapper;

    private OkHttpClient httpClient;

    /**
     * アプリケーション起動時の処理.
     */
    @PostConstruct
    void postConstruct() {
        log.info(Json.createObjectBuilder().add("postConstruct", Json.createObjectBuilder()
                .add("url", url)
                .add("connectTimeout", connectTimeout)
                .add("readTimeout", readTimeout)
                .add("writeTimeout", writeTimeout)
                .add("logLevel", logLevel.toString()))
                .build().toString());

        this.httpClient = newHTTPClient();
    }

    /**
     * @return {@link OkHttpClient} instance
     */
    private OkHttpClient newHTTPClient() {
        final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(logLevel);

        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .addInterceptor(logging)
                .build();
    }

    /**
     * 品番存在チェック.
     *
     * @param partNo 品番
     * @param year 年度
     * @return true : 発注生産システムに品番が存在する, false : 発注生産システムに品番が存在しない
     */
    public boolean existsByPartNoAndYear(final String partNo, final int year) {
        //PRD_0180 jfe mod start
//        final String methodName = "existsByPartNoAndYear";
//
//        log.info(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
//                .add("parameter", Json.createObjectBuilder()
//                        .add("partNo", partNo)
//                        .add("year", year)))
//                .build().toString());
//
//        final boolean result;
//
//        try {
//            final Response response = httpClient.newCall(new Request.Builder().url(
//                    HttpUrl.parse(url).newBuilder()
//                            .addQueryParameter("partNo", partNo)
//                            .addQueryParameter("year", Integer.toString(year))
//                            .build())
//                    .get().build()).execute();
//
//            if (response.isSuccessful()) {
//                result = objectMapper.readValue(response.body().string(), JunpcHinmstExistsByPartNoResultModel.class).getRecordCnt() > 0;
//            } else if (response.code() == HttpStatus.NOT_FOUND.value()) {
//                // 対象リソースが見つからない場合は、データなしとみなす
//                result = false;
//            } else {
//                log.error(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
//                        .add("message", "Error occurred in part number existence check API")
//                        .add("responseCode", response.code()))
//                        .build().toString());
//
//                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_03));
//            }
//        } catch (IOException e) {
//            log.error(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
//                    .add("message", "IOException occurred"))
//                    .build().toString(), e);
//
//            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_I_03), e);
//        }
//
//        log.info(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
//                .add("response", result))
//                .build().toString());
        final boolean result = false;
      //PRD_0180 jfe mod end
        return result;
    }
}
