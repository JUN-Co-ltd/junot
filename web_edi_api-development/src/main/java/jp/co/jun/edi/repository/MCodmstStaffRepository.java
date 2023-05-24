package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import jp.co.jun.edi.entity.MCodmstEntity;

/**
 * 発注生産システムのコードマスタから担当者を検索するリポジトリ.
 */
public interface MCodmstStaffRepository extends JpaRepository<MCodmstEntity, BigInteger>, JpaSpecificationExecutor<MCodmstEntity>  {

}
