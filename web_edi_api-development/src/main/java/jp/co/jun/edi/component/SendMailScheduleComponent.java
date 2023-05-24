package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.entity.TSendMailEntity;
import jp.co.jun.edi.repository.TSendMailRepository;
import jp.co.jun.edi.type.SendMailType;

/**
 * メールの情報取得/更新するコンポーネント.
 */
@Component
public class SendMailScheduleComponent {

    @Autowired
    private TSendMailRepository tSendMailRepository;

    /**
     * メールの情報を取得する.
     * @return メール情報
     */
    public List<TSendMailEntity> getMailInfo() {
        return tSendMailRepository.findBySendStatusUnsend();
    }

    /**
     * 送信状態を変更する.
     * @return 更新回数
     * @param sendStatus 更新後の送信状態
     * @param id 更新するメールのID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int updateSendStatus(final SendMailType sendStatus, final BigInteger id) {
        return tSendMailRepository.updateSendStatus(sendStatus, id);
    }
}
