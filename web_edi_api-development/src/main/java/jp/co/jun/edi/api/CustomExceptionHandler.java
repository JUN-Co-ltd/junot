package jp.co.jun.edi.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.WebUtils;

import jp.co.jun.edi.component.MessageComponent;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.exception.SystemException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ErrorDetailModel;
import jp.co.jun.edi.model.ErrorModel;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * CustomExceptionHandler.
 * {@link org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler} をベースに作成しています。
 */
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @Autowired
    private MessageComponent messageComponent;

    /**
     * 対象データなしの場合のエラーレスポンスを返却します.
     *
     * @param ex instance of {@link ResourceNotFoundException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            final ResourceNotFoundException ex,
            final WebRequest request) {
        final ErrorModel error = toError(createError(HttpStatus.NOT_FOUND, request), ex.getResultMessages());

        log.warn(LogStringUtil.of("handleResourceNotFoundException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * 業務エラーの場合のエラーレスポンスを返却します.
     *
     * @param ex instance of {@link BusinessException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(
            final BusinessException ex,
            final WebRequest request) {
        final ErrorModel error = toError(createError(HttpStatus.BAD_REQUEST, request), ex.getResultMessages());

        log.warn(LogStringUtil.of("handleBusinessException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * バリデーションエラーの場合のエラーレスポンスを返却します.
     *
     * @param ex instance of {@link ValidateException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(ValidateException.class)
    public ResponseEntity<Object> handleValidateException(
            final ValidateException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.BAD_REQUEST, request);
        error.setErrors(ex.getValidateModel().getErrors());

        log.warn(LogStringUtil.of("handleValidateException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * システムエラーの場合のエラーレスポンスを返却します.
     *
     * @param ex instance of {@link SystemException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Object> handleSystemException(
            final SystemException ex,
            final WebRequest request) {
        final ErrorModel error = toError(createError(HttpStatus.INTERNAL_SERVER_ERROR, request), ex);

        log.error(LogStringUtil.of("handleSystemException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link HttpRequestMethodNotSupportedException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(
            final HttpRequestMethodNotSupportedException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.METHOD_NOT_ALLOWED, request);

        final HttpHeaders headers = new HttpHeaders();
        final Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();

        if (!CollectionUtils.isEmpty(supportedMethods)) {
            headers.setAllow(supportedMethods);
        }

        log.warn(LogStringUtil.of("handleHttpRequestMethodNotSupportedException").exception(ex).build());

        return handleExceptionInternal(ex, error, headers, error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link HttpMediaTypeNotSupportedException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupportedException(
            final HttpMediaTypeNotSupportedException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);
        final HttpHeaders headers = new HttpHeaders();
        final List<MediaType> mediaTypes = ex.getSupportedMediaTypes();

        if (!CollectionUtils.isEmpty(mediaTypes)) {
            headers.setAccept(mediaTypes);
        }

        log.warn(LogStringUtil.of("handleHttpMediaTypeNotSupportedException").exception(ex).build());

        return handleExceptionInternal(ex, error, headers, error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link HttpMediaTypeNotAcceptableException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotAcceptableException(
            final HttpMediaTypeNotAcceptableException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.NOT_ACCEPTABLE, request);

        log.warn(LogStringUtil.of("handleHttpMediaTypeNotAcceptableException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link MissingPathVariableException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Object> handleMissingPathVariableException(
            final MissingPathVariableException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.INTERNAL_SERVER_ERROR, request);

        log.error(LogStringUtil.of("handleMissingPathVariableException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link MissingServletRequestParameterException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameterException(
            final MissingServletRequestParameterException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.BAD_REQUEST, request);

        log.warn(LogStringUtil.of("handleMissingServletRequestParameterException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link ServletRequestBindingException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Object> handleServletRequestBindingException(
            final ServletRequestBindingException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.BAD_REQUEST, request);

        log.warn(LogStringUtil.of("handleServletRequestBindingException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link ConversionNotSupportedException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(ConversionNotSupportedException.class)
    public ResponseEntity<Object> handleConversionNotSupportedException(
            final ConversionNotSupportedException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.INTERNAL_SERVER_ERROR, request);

        log.error(LogStringUtil.of("handleConversionNotSupportedException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link TypeMismatchException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatchException(
            final TypeMismatchException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.BAD_REQUEST, request);

        log.warn(LogStringUtil.of("handleTypeMismatchException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link HttpMessageNotReadableException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(
            final HttpMessageNotReadableException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.BAD_REQUEST, request);

        log.warn(LogStringUtil.of("handleHttpMessageNotReadableException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link HttpMessageNotWritableException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Object> handleHttpMessageNotWritableException(
            final HttpMessageNotWritableException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.INTERNAL_SERVER_ERROR, request);

        log.error(LogStringUtil.of("handleHttpMessageNotWritableException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * バリデーションエラーの例外.
     *
     * @param ex instance of {@link MethodArgumentNotValidException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex,
            final WebRequest request) {
        final ErrorModel error = toError(createError(HttpStatus.BAD_REQUEST, request), ex.getBindingResult());

        log.warn(LogStringUtil.of("handleMethodArgumentNotValidException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link MissingServletRequestPartException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingServletRequestPartException(
            final MissingServletRequestPartException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.BAD_REQUEST, request);

        log.warn(LogStringUtil.of("handleMissingServletRequestPartException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * バインドエラーの例外.
     *
     * @param ex instance of {@link BindException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(
            final BindException ex,
            final WebRequest request) {
        final ErrorModel error = toError(createError(HttpStatus.BAD_REQUEST, request), ex.getBindingResult());

        log.warn(LogStringUtil.of("handleBindException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link NoHandlerFoundException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFoundException(
            final NoHandlerFoundException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.NOT_FOUND, request);

        log.warn(LogStringUtil.of("handleNoHandlerFoundException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * @param ex instance of {@link AsyncRequestTimeoutException}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<Object> handleAsyncRequestTimeoutException(
            final AsyncRequestTimeoutException ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.SERVICE_UNAVAILABLE, request);
        final ServletWebRequest servletWebRequest = getServletWebRequest(request);

        if (Objects.nonNull(servletWebRequest)) {
            final HttpServletResponse response = servletWebRequest.getResponse();

            if (Objects.nonNull(response) && response.isCommitted()) {
                if (log.isDebugEnabled()) {
                    final HttpServletRequest httpServletRequest = servletWebRequest.getRequest();

                    log.debug(LogStringUtil.of("handleAsyncRequestTimeoutException")
                            .message("async timeout.")
                            .value("method", httpServletRequest.getMethod())
                            .value("requestURI", httpServletRequest.getRequestURI())
                            .build());
                }

                return null;
            }
        }

        log.warn(LogStringUtil.of("handleAsyncRequestTimeoutException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * AccessDeniedException発生時のエラーレスポンスを返却します.
     *
     * @param ex instance of {@link Exception}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            final Exception ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.FORBIDDEN, request);
        log.warn(LogStringUtil.of("handleAccessDeniedException").exception(ex).build());
        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * org.apache.catalina.connector.ClientAbortException発生時は、クライアントで処理が中断されているため、レスポンスは返却しません.
     *
     * <p>サーブレット・コンテナからレスポンスを返却する際にサーブレット・ゲートウェイとサーブレット・コンテナ間が切断されていたときに発生します。</p>
     * <p>発生原因としては以下のことが考えられます。</p>
     *
     * <pre>
     * - クライアントで処理が中断された。
     * - クライアントからリクエストを送信したあと、レスポンスが返ってくる前に同クライアントより再びリクエストが送信された。
     * - サーブレット・ゲートウェイでタイムアウトが発生した。
     * - クライアントでブラウザが強制停止された。
     * - クライアントでリロードボタン、同じリンク先のボタンなどが連打された。
     * </pre>
     *
     * @param ex instance of {@link org.apache.catalina.connector.ClientAbortException}
     */
    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    public void handleClientAbortException(
            final org.apache.catalina.connector.ClientAbortException ex) {
        log.warn(LogStringUtil.of("handleClientAbortException").exception(ex).build());
    }

    /**
     * Exception発生時のエラーレスポンスを返却します.
     *
     * @param ex instance of {@link Exception}
     * @param request instance of {@link WebRequest}
     * @return instance of {@link ResponseEntity}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(
            final Exception ex,
            final WebRequest request) {
        final ErrorModel error = createError(HttpStatus.INTERNAL_SERVER_ERROR, request);

        log.error(LogStringUtil.of("handleException").exception(ex).build());

        return handleExceptionInternal(ex, error, new HttpHeaders(), error.getHttpStatus(), request);
    }

    /**
     * システムエラーメッセージをエラーレスポンスに変換します.
     *
     * @param error instance of {@link ErrorModel}
     * @param ex instance of {@link SystemException}
     * @return {@link ErrorModel} instance
     */
    private ErrorModel toError(
            final ErrorModel error,
            final SystemException ex) {
        final List<ErrorDetailModel> list = new ArrayList<>();
        final ErrorDetailModel errorDetail = new ErrorDetailModel();

        errorDetail.setCode(ex.getCode());

        list.add(errorDetail);

        error.setErrors(list);

        return error;
    }

    /**
     * 結果メッセージをエラーレスポンスに変換します.
     *
     * @param error instance of {@link ErrorModel}
     * @param resultMessages instance of {@link ResultMessages}
     * @return {@link ErrorModel} instance
     */
    private ErrorModel toError(
            final ErrorModel error,
            final ResultMessages resultMessages) {
        if (Objects.nonNull(resultMessages)
                && resultMessages.isNotEmpty()) {
            error.setErrors(messageComponent.toErrorDetails(resultMessages.getList()));
        }

        return error;
    }

    /**
     * 検証結果をエラー情報に変換します.
     *
     * @param error instance of {@link ErrorModel}
     * @param bindingResult instance of {@link BindingResult}
     * @return {@link ErrorModel} instance
     */
    private ErrorModel toError(
            final ErrorModel error,
            final BindingResult bindingResult) {
        if (Objects.nonNull(bindingResult)
                && bindingResult.hasFieldErrors()) {
            error.setErrors(bindingResult.getFieldErrors().stream().map(fieldError -> toErrorDetail(fieldError)).collect(Collectors.toList()));
        }

        return error;
    }

    /**
     * 項目検証結果をエラー詳細に変換します.
     *
     * @param fieldError instance of {@link FieldError}
     * @return {@link ErrorDetailModel} instance
     */
    private ErrorDetailModel toErrorDetail(
            final FieldError fieldError) {
        final ErrorDetailModel errorDetail = new ErrorDetailModel();

        errorDetail.setCode(MessageCodeType.CODE_004);
        errorDetail.setMessage(fieldError.getDefaultMessage());

        return errorDetail;
    }

    /**
     * エラー情報を生成します.
     *
     * @param status instance of {@link HttpStatus}
     * @return {@link ErrorModel} instance
     */
    private ErrorModel createError(
            final HttpStatus status) {
        return new ErrorModel(new Date(), status, null);
    }

    /**
     * エラー情報を生成します.
     *
     * @param status instance of {@link HttpStatus}
     * @param webRequest instance of {@link WebRequest}
     * @return {@link ErrorModel} instance
     */
    private ErrorModel createError(
            final HttpStatus status,
            final WebRequest webRequest) {
        final ServletWebRequest servletWebRequest = getServletWebRequest(webRequest);

        if (Objects.nonNull(servletWebRequest)) {
            return createError(status, servletWebRequest.getRequest());
        }

        return createError(status);
    }

    /**
     * エラー情報を生成します.
     *
     * @param status instance of {@link HttpStatus}
     * @param httpServletRequest instance of {@link HttpServletRequest}
     * @return {@link ErrorModel} instance
     */
    private ErrorModel createError(
            final HttpStatus status,
            final HttpServletRequest httpServletRequest) {
        if (Objects.nonNull(httpServletRequest)) {
            return new ErrorModel(new Date(), status, httpServletRequest.getRequestURI());
        }

        return createError(status);
    }

    /**
     * webRequestを、ServletWebRequestクラスに変換します.
     *
     * @param webRequest instance of {@link WebRequest}
     * @return instance of {@link ServletWebRequest}
     */
    private ServletWebRequest getServletWebRequest(
            final WebRequest webRequest) {
        if (webRequest instanceof ServletWebRequest) {
            return (ServletWebRequest) webRequest;
        }

        return null;
    }

    /**
     * A single place to customize the response body of all Exception types.
     * <p>The default implementation sets the {@link WebUtils#ERROR_EXCEPTION_ATTRIBUTE}
     * request attribute and creates a {@link ResponseEntity} from the given
     * body, headers, and status.
     * @param ex the exception
     * @param body the body for the response
     * @param headers the headers for the response
     * @param status the response status
     * @param request the current request
     * @return instance of {@link ResponseEntity}
     */
    protected ResponseEntity<Object> handleExceptionInternal(
            final Exception ex,
            final Object body,
            final HttpHeaders headers,
            final HttpStatus status,
            final WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }

        return new ResponseEntity<>(body, headers, status);
    }
}
