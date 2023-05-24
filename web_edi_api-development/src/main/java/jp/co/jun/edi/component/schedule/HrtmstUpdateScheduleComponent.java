
// #PRD_0138 #10680 JFE add start
package jp.co.jun.edi.component.schedule;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.repository.MHrtmstRepository;



/**
 * 配分率マスタ更新バッチ用コンポーネント.
 */

@Component

public class HrtmstUpdateScheduleComponent {

	@Autowired
	private MHrtmstRepository MHrtmstRepository;

	@Autowired
	private jp.co.jun.edi.repository.MStoreHrtmstRepository MStoreHrtmstRepository;

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteInsertByHaibunSummaryTarget(String nitymd,String adminName, BigInteger userId) {
		MHrtmstRepository.deleteByHaibunSummaryTarget(nitymd);
		MHrtmstRepository.insertByHaibunSummaryTarget(nitymd,adminName,userId);
	}

	public Integer countByNitymd(String nitymd) {
		return MStoreHrtmstRepository.countByNitymd(nitymd);
	}

}
// #PRD_0138 #10680 JFE add end
