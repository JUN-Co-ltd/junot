package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import jp.co.jun.edi.entity.converter.OrderCategoryTypeConverter;
import jp.co.jun.edi.type.OrderCategoryType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張発注メーカー情報のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTOrderSupplierEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** メーカーコード. */
    @Column(name = "supplier_code")
    private String supplierCode;

    /** メーカー名. */
    @Column(name = "supplier_name")
    private String supplierName;

    /** 発注分類区分. */
    @Convert(converter = OrderCategoryTypeConverter.class)
    @Column(name = "order_category_type")
    private OrderCategoryType orderCategoryType;

    /** 工場コード. */
    @Column(name = "supplier_factory_code")
    private String supplierFactoryCode;

    /** 工場名. */
    @Column(name = "supplier_factory_name")
    private String supplierFactoryName;

    /** 委託先工場名. */
    @Column(name = "consignment_factory")
    private String consignmentFactory;

    /** メーカー担当ID. */
    @Column(name = "supplier_staff_id")
    private BigInteger supplierStaffId;

}
