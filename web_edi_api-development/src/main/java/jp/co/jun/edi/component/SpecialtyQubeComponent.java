package jp.co.jun.edi.component;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.model.SpecialtyQubeCancelResponseXmlModel;
import jp.co.jun.edi.component.model.SpecialtyQubeDeleteResponseXmlModel;
import jp.co.jun.edi.component.model.SpecialtyQubeRequestXmlModel;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.type.MessageCodeType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * SpecialtyQubeAPI関連のコンポーネント.
 */
@Component
@Slf4j
public class SpecialtyQubeComponent extends GenericComponent {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".component.specialty-qube-component";

    /** URL. */
    @Value("${" + PROPERTY_NAME_PREFIX + ".cancel-url}")
    private String cancelUrl;

    /** URL. */
    @Value("${" + PROPERTY_NAME_PREFIX + ".delete-url}")
    private String deleteUrl;

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

    private OkHttpClient httpClient;

    /**
     * アプリケーション起動時の処理.
     */
    @PostConstruct
    void postConstruct() {
        log.info(Json.createObjectBuilder().add("postConstruct", Json.createObjectBuilder()
                .add("cancelUrl", cancelUrl)
                .add("deleteUrl", deleteUrl)
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
     * 店別配分キャンセルAPI実行.
     *
     * @param reqXmlModel リクエストパラメータ
     * @return レスポンス
     */
    public SpecialtyQubeCancelResponseXmlModel executeCancel(final SpecialtyQubeRequestXmlModel reqXmlModel) {
        final String methodName = "executeCancel";

        final StringWriter reqXml = new StringWriter();
        JAXB.marshal(reqXmlModel, reqXml);

        log.info(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
                .add("parameter", reqXml.toString()))
                .build().toString());

        final RequestBody requestBody = RequestBody.create(MediaType.parse("text/xml; charset=utf-8"), reqXml.toString());
        SpecialtyQubeCancelResponseXmlModel result = null;

        try {
            final Response response = httpClient.newCall(new Request.Builder().url(
                    HttpUrl.parse(cancelUrl).newBuilder().build()).post(requestBody).build()).execute();
            if (!response.isSuccessful()) {
                log.error(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
                        .add("message", "Error occurred in part number existence check API")
                        .add("responseCode", response.code()))
                        .build().toString());
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_S_001));
            }
            result = JAXB.unmarshal(new StringReader(response.body().string()), SpecialtyQubeCancelResponseXmlModel.class);
        } catch (IOException e) {
            log.error(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
                    .add("message", "IOException occurred"))
                    .build().toString(), e);

            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_S_001), e);
        }

        String resultStr = "";
        if (result != null) {
            resultStr = result.toString();
        }
        log.info(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
                .add("response", resultStr))
                .build().toString());

        return result;
    }

    /**
     * 全店配分削除API実行.
     *
     * @param reqXmlModel リクエストパラメータ
     * @return レスポンス
     */
    public SpecialtyQubeDeleteResponseXmlModel executeDelete(final SpecialtyQubeRequestXmlModel reqXmlModel) {
        final String methodName = "executeDelete";

        final StringWriter reqXml = new StringWriter();
        JAXB.marshal(reqXmlModel, reqXml);

        log.info(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
                .add("parameter", reqXml.toString()))
                .build().toString());

        final RequestBody requestBody = RequestBody.create(MediaType.parse("text/xml; charset=utf-8"), reqXml.toString());
        SpecialtyQubeDeleteResponseXmlModel result = null;

        try {
            final Response response = httpClient.newCall(new Request.Builder().url(
                    HttpUrl.parse(deleteUrl).newBuilder().build()).post(requestBody).build()).execute();
            if (!response.isSuccessful()) {
                log.error(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
                        .add("message", "Error occurred in part number existence check API")
                        .add("responseCode", response.code()))
                        .build().toString());
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_S_001));
            }

            result = JAXB.unmarshal(new StringReader(response.body().string()), SpecialtyQubeDeleteResponseXmlModel.class);
        } catch (IOException e) {
            log.error(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
                    .add("message", "IOException occurred"))
                    .build().toString(), e);

            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_S_001), e);
        }

        String resultStr = "";
        if (result != null) {
            resultStr = result.toString();
        }
        log.info(Json.createObjectBuilder().add(methodName, Json.createObjectBuilder()
                .add("response", resultStr))
                .build().toString());

        return result;
    }
}
