//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.PurchaseRecordCsvEntity;
import jp.co.jun.edi.repository.custom.PurchaseRecordCsvCompositeRepositoryCustom;

/**
 * 仕入実績一覧情報Repository.
 */
@Repository
public interface PurchaseRecordCsvCompositeRepository extends CrudRepository<PurchaseRecordCsvEntity, Integer>, PurchaseRecordCsvCompositeRepositoryCustom {
}
//PRD_0133 #10181 add JFE end