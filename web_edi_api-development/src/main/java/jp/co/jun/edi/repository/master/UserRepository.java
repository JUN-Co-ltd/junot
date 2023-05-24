package jp.co.jun.edi.repository.master;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.master.UserEntity;
import jp.co.jun.edi.repository.master.custom.UserRepositoryCustom;

/**
 * ユーザ情報Repository.
 */
@Repository
public interface UserRepository extends CrudRepository<UserEntity, Integer>, UserRepositoryCustom {
}
