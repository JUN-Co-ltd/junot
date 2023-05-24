package jp.co.jun.edi.api.v1;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jp.co.jun.edi.exception.SystemException;
import jp.co.jun.edi.model.FileModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.FileCreateService;
import jp.co.jun.edi.service.FileDeleteService;
import jp.co.jun.edi.service.FileGetService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.type.MessageCodeType;
import lombok.extern.slf4j.Slf4j;

/**
 * ファイルAPI.
 */
@RestController
@RequestMapping("/api/v1/files")
@Slf4j
public class FileV1Api {
    @Autowired
    private FileCreateService createService;

    @Autowired
    private FileGetService getService;

    @Autowired
    private FileDeleteService deleteService;

    /**
     * ファイルを作成します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param multipartFile {@link MultipartFile} instance
     * @return {@link FileModel} instance
     */
    @PostMapping
    public FileModel create(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @RequestParam("file") final MultipartFile multipartFile) {
        log.info("call() execution param. {\"OriginalFilename\":\"" + multipartFile.getOriginalFilename() + "\"}");

        final FileModel file = new FileModel();
        // ファイルIDのみを返却する
        file.setId(createService.call(CreateServiceParameter.<FileModel>builder().loginUser(loginUser).item(toFile(multipartFile)).build()).getItem().getId());

        log.info("call() return param. {omitted}");

        return file;
    }

    /**
     * ファイルを削除します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param fileId ファイルID
     */
    @DeleteMapping("/{fileId}")
    public void delete(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("fileId") final BigInteger fileId) {
        deleteService.call(DeleteServiceParameter.<BigInteger>builder().loginUser(loginUser).id(fileId).build());

        return;
    }

    /**
     * ファイルを取得します.
     *
     * @param loginUser {@link CustomLoginUser} instance
     * @param fileId ファイルID
     * @return {@link HttpEntity} instance
     */
    @GetMapping("/{fileId}")
    public HttpEntity<byte[]> get(
            @AuthenticationPrincipal final CustomLoginUser loginUser,
            @PathVariable("fileId") final BigInteger fileId) {
        log.info("call() execution param. {\"fileId\":\"" + fileId + "\"}");

        final FileModel file = getService.call(GetServiceParameter.<BigInteger>builder().id(fileId).build()).getItem();

        String encFileName;
        try {
            encFileName = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 not supported");
        }

        final MediaType mediaType = MediaType.valueOf(file.getContentType());
        final byte[] fileByteArray = file.getFileData();
        ContentDisposition contentDisposition = ContentDisposition.parse("attachment; filename=" + encFileName);

        log.info("call() return param. {omitted}");
        return new HttpEntity<byte[]>(fileByteArray, toHttpHeaders(mediaType, fileByteArray.length, contentDisposition));
    }

    /**
     * @param multipartFile {@link MultipartFile} instance
     * @return {@link FileModel} instance
     */
    private FileModel toFile(
            final MultipartFile multipartFile) {
        final FileModel file = new FileModel();

        file.setFileName(multipartFile.getOriginalFilename());
        file.setContentType(multipartFile.getContentType());

        try {
            file.setFileData(multipartFile.getBytes());
        } catch (IOException e) {
            throw new SystemException(MessageCodeType.SYSTEM_ERROR, e);
        }

        return file;
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
