package jp.co.jun.edi.service;

import jp.co.jun.edi.exception.ResultMessagesNotificationException;
import jp.co.jun.edi.exception.SystemException;
import jp.co.jun.edi.model.GenericModel;
import jp.co.jun.edi.service.parameter.ValidateServiceParameter;
import jp.co.jun.edi.service.response.ValidateServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * @param <PARAMETER>
 * @param <RESPONSE>
 */
public abstract class GenericValidateService<PARAMETER extends GenericModel, RESPONSE extends GenericModel> extends GenericService {
    /**
     * @param serviceParameter {@link ValidateServiceParameter} instance
     * @return {@link ValidateServiceResponse} instance
     */
    public ValidateServiceResponse<RESPONSE> call(final ValidateServiceParameter<PARAMETER> serviceParameter) {
        final long start = System.currentTimeMillis();

        try {
            final ValidateServiceParameter<PARAMETER> parameter = serviceParameter;

            getLog().info("call() start. execution param. {}", toJsonString(parameter.getLogObject()));

            final ValidateServiceResponse<RESPONSE> response = execute(parameter);

            getLog().info("call() end in {} ms. return param. {}", System.currentTimeMillis() - start, toJsonString(response.getLogObject()));

            return response;
        } catch (ResultMessagesNotificationException e) {
            getLog().warn("call() end in " + (System.currentTimeMillis() - start) + " ms. ResultMessagesNotificationException occurred.", e);

            throw e;
        } catch (Throwable e) {
            getLog().error("call() end in " + (System.currentTimeMillis() - start) + " ms. Throwable occurred.", e);

            throw new SystemException(MessageCodeType.SYSTEM_ERROR, e);
        }
    }

    /**
     * Serviceを実行する.
     *
     * @param serviceParameter {@link ValidateServiceParameter} instance
     * @return {@link ValidateServiceResponse} instance
     */
    protected abstract ValidateServiceResponse<RESPONSE> execute(ValidateServiceParameter<PARAMETER> serviceParameter);
}
