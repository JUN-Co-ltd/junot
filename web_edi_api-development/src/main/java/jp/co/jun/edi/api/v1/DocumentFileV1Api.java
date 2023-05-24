package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.AmazonServiceException;

import jp.co.jun.edi.component.S3Component;
import jp.co.jun.edi.config.PropertyName;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * ファイルAPI.
 */
@RestController
@RequestMapping("/api/v1/documentFiles")
@Slf4j
public class DocumentFileV1Api {
    private static final String PROPERTY_NAME_PREFIX = PropertyName.ROOT + ".api.v1.document-file";

    @Autowired
    private S3Component s3Component;

    @Value("${" + PROPERTY_NAME_PREFIX + ".s3-share-prefix}")
    private String s3SharePrefix;

    @Value("${" + PROPERTY_NAME_PREFIX + ".s3-jun-prefix}")
    private String s3JunPrefix;

    /**
     * 共有ドキュメントを取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param fileName ファイル名
     * @return {@link HttpEntity} instance
     */
    @GetMapping("/share/{fileName}")
    public HttpEntity<byte[]> share(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("fileName") final String fileName) {
        return toHttpEntity(s3SharePrefix + fileName, fileName, new MimetypesFileTypeMap().getContentType(fileName));
    }

    /**
     * JUN権限のみアクセス可能なドキュメントを取得する.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param fileName ファイル名
     * @return {@link HttpEntity} instance
     */
    @Secured("ROLE_JUN")
    @GetMapping("/jun/{fileName}")
    public HttpEntity<byte[]> jun(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("fileName") final String fileName) {
        return toHttpEntity(s3JunPrefix + fileName, fileName, new MimetypesFileTypeMap().getContentType(fileName));
    }

    /**
     * ファイル名をエンコードする.
     *
     * @param fileName ファイル名
     * @return URLエンコード後のファイル名
     */
    private String encodeFileName(final String fileName) {
        try {
            return URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.warn(LogStringUtil.of("encodeFileName").exception(e).build());
            throw new AssertionError("UTF-8 not supported");
        }
    }

    /**
     * HttpEntity型に変換する.
     *
     * @param s3Key S3のキー
     * @param fileName ファイル名
     * @param mediaType メディアタイプ
     * @return {@link HttpEntity} instance
     */
    private HttpEntity<byte[]> toHttpEntity(
            final String s3Key,
            final String fileName,
            final String mediaType) {
        final byte[] fileByteArray = fileDownload(s3Key);

        return new HttpEntity<byte[]>(fileByteArray, toHttpHeaders(
                MediaType.valueOf(mediaType),
                fileByteArray.length,
                ContentDisposition.parse("attachment; filename=" + encodeFileName(fileName))));
    }

    /**
     * S3からファイルをダウンロードする.
     *
     * @param s3Key S3のキー
     * @return ファイルの実態
     */
    private byte[] fileDownload(
            final String s3Key) {
        try {
            return s3Component.download(s3Key);
        } catch (IOException e) {
            throw new ResourceNotFoundException(e);
        } catch (AmazonServiceException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    /**
     * HttpHeaders型に変換する.
     *
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

        final List<String> exposedHeaders = new ArrayList<>();

        exposedHeaders.add("Content-Disposition");
        headers.setAccessControlExposeHeaders(exposedHeaders);

        return headers;
    }
}
