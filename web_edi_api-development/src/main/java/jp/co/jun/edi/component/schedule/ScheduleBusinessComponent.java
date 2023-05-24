package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.PropertyComponent;
import jp.co.jun.edi.exception.ScheduleException;
import jp.co.jun.edi.repository.MUserRepository;
import jp.co.jun.edi.util.LogStringUtil;

/**
 * スケジュールバッチのビジネスコンポーネント.
 */
@Component
public class ScheduleBusinessComponent {

    @Autowired
    private PropertyComponent propertyComponent;
    @Autowired
    private MUserRepository mUserRepository;
    /**
     * ユーザIDを取得する.
     * @return ユーザID
     */
    public BigInteger getUserId() {
        final String accountName = propertyComponent.getCommonProperty().getAdminUserAccountName();
        final String company = propertyComponent.getCommonProperty().getAdminUserCompany();

        return mUserRepository.findByAccountNameAndCompanyIgnoreSystemManaged(accountName, company).orElseThrow(
                () -> new ScheduleException(LogStringUtil.of("getUserId")
                        .message("m_user not found.")
                        .value("accountName", accountName)
                        .value("company", company)
                        .build()))
                .getId();
    }

}
