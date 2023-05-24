package jp.co.jun.edi.service;

import java.util.Objects;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.exception.ResultMessagesNotificationException;
import jp.co.jun.edi.exception.SystemException;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * @param <PARAMETER>
 * @param <RESPONSE>
 */
public abstract class GenericDeleteService<PARAMETER extends GenericServiceParameter, RESPONSE extends GenericServiceResponse> extends GenericService {
    /**
     * @param serviceParameter Service Parameter
     * @return Serviceレスポンス
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = { ResultMessagesNotificationException.class, SystemException.class })
    public RESPONSE call(final PARAMETER serviceParameter) {
        final long start = System.currentTimeMillis();

        try {
            final PARAMETER parameter = Objects.requireNonNull(serviceParameter);

            getLog().info("call() start. execution param. {}", toJsonString(parameter));

            final RESPONSE response = Objects.requireNonNull(execute(parameter));

            getLog().info("call() end in {} ms. return param. {}", System.currentTimeMillis() - start, toJsonString(response));

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
     * @param serviceParameter Service Parameter
     * @return <RESPONSE>
     */
    protected abstract RESPONSE execute(PARAMETER serviceParameter);
}
