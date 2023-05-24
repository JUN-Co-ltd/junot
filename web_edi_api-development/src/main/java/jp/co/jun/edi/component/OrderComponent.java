package jp.co.jun.edi.component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.mail.StackTOrderOfficialSendMailComponent;
import jp.co.jun.edi.component.mail.StackTOrderRequestSendMailComponent;
import jp.co.jun.edi.component.mail.StackTOrderSendMailComponent;
import jp.co.jun.edi.component.model.ItemChangeStateModel;
import jp.co.jun.edi.component.model.OrderChangeStateModel;
import jp.co.jun.edi.entity.TItemEntity;
import jp.co.jun.edi.entity.TOrderEntity;
import jp.co.jun.edi.entity.TOrderSkuEntity;
import jp.co.jun.edi.entity.constants.EndAtTypeConstants;
import jp.co.jun.edi.entity.extended.ExtendedTItemEntity;
import jp.co.jun.edi.entity.extended.ExtendedTOrderEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.ItemModel;
import jp.co.jun.edi.model.OrderModel;
import jp.co.jun.edi.model.OrderSkuModel;
import jp.co.jun.edi.model.OrderSupplierModel;
import jp.co.jun.edi.repository.TDeliveryRepository;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TOrderSkuRepository;
import jp.co.jun.edi.repository.TOrderSupplierRepository;
import jp.co.jun.edi.repository.extended.ExtendedTItemRepository;
import jp.co.jun.edi.repository.extended.ExtendedTOrderRepository;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.type.ApprovalType;
import jp.co.jun.edi.type.CompleteOrderType;
import jp.co.jun.edi.type.CompleteType;
import jp.co.jun.edi.type.ExpenseItemType;
import jp.co.jun.edi.type.LinkingStatusType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.type.OrderApprovalType;
import jp.co.jun.edi.type.OrderCategoryType;
import jp.co.jun.edi.type.OrderSheetOutType;
import jp.co.jun.edi.type.QualityApprovalType;
import jp.co.jun.edi.type.SendType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 発注関連のコンポーネント.
 */
@Component
public class OrderComponent extends GenericComponent {

    @Autowired
    private TOrderRepository orderRepository;

    @Autowired
    private TOrderSkuRepository orderSkuRepository;

    @Autowired
    private MKanmstComponent mKanmstComponent;

    @Autowired
    private ExtendedTItemRepository extendedTItemRepository;

    @Autowired
    private ExtendedTOrderRepository extendedTOrderRepository;

    @Autowired
    private TDeliveryRepository deliveryRepository;

    @Autowired
    private StackTOrderSendMailComponent stackTOrderSendMailComponent;

    @Autowired
    private StackTOrderOfficialSendMailComponent stackTOrderOfficialSendMailComponent;

    @Autowired
    private LoginUserComponent loginUserComponent;

    @Autowired
    private StackTOrderRequestSendMailComponent stackTOrderRequestSendMailComponent;

    @Autowired
    private TOrderSupplierRepository tOrderSupplierRepository;

    @Autowired
    private MisleadingRepresentationComponent misleadingRepresentationComponent;

    @Autowired
    private TItemRepository itemRepository;

//  PRD_0142 #10423 JFE add start
    /** TAGDAT作成フラグ（未作成）. */
	private static final String NOT_CREATED = "0";
//  PRD_0142 #10423 JFE add end

    /**
     * 発注情報を取得する.
     *
     * @param id 発注ID
     * @return 発注情報
     */
    public ExtendedTOrderEntity getExtendedTOrder(final BigInteger id) {
        return extendedTOrderRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
    }

    /**
     * 発注SKU情報リストを取得する.
     *
     * @param orderId 発注ID
     * @return 発注SKU情報リスト
     */
    public List<TOrderSkuEntity> getTOrderSkus(final BigInteger orderId) {
        final PageRequest page = PageRequest.of(0, Integer.MAX_VALUE);
        final Page<TOrderSkuEntity> result = orderSkuRepository.findByOrderId(orderId, page);
        if (!result.hasContent()) {
           throw new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002));
        }
        return result.getContent();
    }

    /**
     * 発注情報の変更状態を取得する.
     *
     * @param order 更新された発注情報
     * @param tOrder DB上の発注情報
     * @return 発注情報の変更状態
     */
    public OrderChangeStateModel getOrderChangeState(final OrderModel order, final TOrderEntity tOrder) {
        final OrderChangeStateModel orderChangeState = new OrderChangeStateModel();

        // MD承認済→未承認となる変更有無を判定
        orderChangeState.setUnapprovedTargetChanged(isUnapprovedTargetChanged(order, tOrder));

        return orderChangeState;
    }

    /**
     * MD承認済→未承認となる変更有無を判定する.
     *
     * <p>MD承認済の場合、以下のいずれかの項目が変更された場合、MD承認済→未承認となる。</p>
     *
     * <pre>
     * - 上代
     * - 原価（下代）
     * - 数量
     * </pre>
     *
     * @param order 更新された発注情報
     * @param tOrder DB上の発注情報
     * @return 判定結果
     *  <pre>
     *  - true : 変更あり
     *  - false : 変更なし
     *  </pre>
     */
    private boolean isUnapprovedTargetChanged(final OrderModel order, final TOrderEntity tOrder) {
        if (!isOrderApproved(tOrder)) {
            // MD承認済以外の場合は、変更なし
            return false;
        }

        return !Objects.equals(order.getRetailPrice(), tOrder.getRetailPrice())
                || !Objects.equals(order.getOtherCost(), tOrder.getOtherCost())
                || !Objects.equals(BigDecimal.valueOf(sumProductOrderLot(order.getOrderSkus())), tOrder.getQuantity());
    }

    /**
     * 発注承認が可否を判定する.
     *
     * <p>発注承認が可能な状態か否かを、承認ステータスから判定する.</p>
     *
     * <pre>
     * - 0:未承認      →false
     * - 1:MD承認      →false
     * - 2:MD差し戻し  →true
     * - 3:発注確定    →true
     * - 4:発注差し戻し→false
     * </pre>
     *
     * @param tOrder DB上の発注情報
     * @return 判定結果
     *  <pre>
     *  - true : 承認可能
     *  - false : 承認不可
     *  </pre>
     */
    public boolean canOrderApproved(final TOrderEntity tOrder) {
        final OrderApprovalType orderApproveStatus = OrderApprovalType.convertToType(tOrder.getOrderApproveStatus());
        return OrderApprovalType.CONFIRM.equals(orderApproveStatus)
                || OrderApprovalType.REJECT.equals(orderApproveStatus);
    }

    /**
     * ログインユーザに発注承認の権限があるかを判定する.
     *
     * @param orderApprovalAuthorityBlands ログインユーザの持つ承認権限リスト
     * @param orderBrand 対象の発注に紐づく品番の持つブランドコード
     * @return 判定結果
     *  <pre>
     *  - true : 権限あり
     *  - false : 権限なし
     *  </pre>
     */
    public boolean canUserApproved(final List<String> orderApprovalAuthorityBlands, final String orderBrand) {
        // ユーザが持つ承認権限の一覧に、
        // 発注の品番に紐づくブランドコードがあるor全権限コード"ZZ"がある場合はtrue
        return orderApprovalAuthorityBlands.contains(orderBrand) || orderApprovalAuthorityBlands.contains("ZZ");
    }

    /**
     * 発注確定済チェック.
     *
     * 発注承認ステータスが
     * MD承認済かつ受注確定済(1)
     * または
     * MD承認差し戻し、受注確定済(2)
     * または
     * 受注確定済、MD未承認(3)
     * の場合、発注確定済み
     *
     * @param orderApproveStatus 発注承認ステータス
     * @return true : 発注確定済み, false : 発注未確定
     */
    public boolean isOrderConfirmed(final String orderApproveStatus) {
        return OrderApprovalType.APPROVED.getValue().equals(orderApproveStatus)
                || OrderApprovalType.REJECT.getValue().equals(orderApproveStatus)
                || OrderApprovalType.CONFIRM.getValue().equals(orderApproveStatus);
    }

    /**
     * MD承認済判定.
     *
     * 発注承認ステータスが
     * MD承認済かつ受注確定済(1)
     * の場合、MD承認済
     *
     * @param tOrder 発注情報
     * @return 判定結果
     *  <pre>
     *  - true : MD承認済
     *  - false : MD未承認
     *  </pre>
     */
    public boolean isOrderApproved(final TOrderEntity tOrder) {
        final OrderApprovalType orderApproveStatus = OrderApprovalType.convertToType(tOrder.getOrderApproveStatus());

        return OrderApprovalType.APPROVED == orderApproveStatus;
    }

    /**
     * 品番IDに紐づく全ての発注の承認状態チェック.
     *
     * 承認済みが1件でもあればtrueを返却
     *
     * @param orderList 最新のDBの発注情報リスト
     * @return true : 承認済みあり, false : 未承認
     */
    public boolean isApprovedOrderList(final List<TOrderEntity> orderList) {
        return orderList.stream().anyMatch(order -> isOrderApproved(order));
    }

    /**
     * 完納状態チェック.
     *
     * 全済区分が済(0)
     * または
     * 製品完納区分が完納(6)または自動完納(5)
     * の場合、完納
     *
     * @param orderEntity 最新のDBの発注情報
     * @return true : 完納, false : 未完納
     */
    public boolean isCompleteOrder(final TOrderEntity orderEntity) {
        return CompleteType.COMPLETE.equals(orderEntity.getAllCompletionType())
                || CompleteOrderType.AUTO_COMPLETE.equals(orderEntity.getProductCompleteOrder())
                || CompleteOrderType.COMPLETE.equals(orderEntity.getProductCompleteOrder());
    }

    /**
     * 完納状態チェック.
     * ※配分一覧で使用
     *
     * 全済区分が済(0)
     * または
     * 製品完納区分が完納(6)または自動完納(5)
     * の場合、完納
     *
     * @param productCompleteOrder 製品完納区分
     * @param allCompletionType 全済区分
     * @return true : 完納, false : 未完納
     */
    public boolean isCompleteOrder(final CompleteOrderType productCompleteOrder, final CompleteType allCompletionType) {
        return CompleteType.COMPLETE.equals(allCompletionType)
                || CompleteOrderType.AUTO_COMPLETE.equals(productCompleteOrder)
                || CompleteOrderType.COMPLETE.equals(productCompleteOrder);
    }

    /**
     * 品番IDに紐づく全ての発注の完納状態チェック.
     *
     * 全ての発注が完納であればtrueを返却
     *
     * @param orderList 最新のDBの発注情報リスト
     * @return true : 全て完納, false : 1つでも未完納、または発注がない
     */
    public boolean isCompleteOrderList(final List<TOrderEntity> orderList) {
        if (CollectionUtils.isEmpty(orderList)) {
            // 発注がない場合、falseを返却
            return false;
        }

        return orderList.stream().allMatch(order -> isCompleteOrder(order));
    }

    /**
     * 発注書の再印刷対象の更新有無を判定する.
     *
     * <p>発注書のPDFに印字されている項目が変更された場合に、発注書の再印刷対象とし、発注生産に連携する。</p>
     * <p>以下のいずれかの条件に該当する場合は、発注書の再印刷対象とする。</p>
     *
     * <pre>
     * - 原産国が変更された。
     *   - 初回納品依頼の承認済、または全ての発注が完納の場合は、原産国は変更不可。
     *   - 優良誤認の「対象」から「対象外」へ、原産国が変更された場合でも、再印刷対象とする。
     * - カラーコードが最小値の色の組成が変更された。（発注生産に連携している色の組成）
     *   - 初回納品依頼の承認済、または全ての発注が完納の場合は、登録済みの色の組成は変更不可。（色の追加は可能）
     *   - 優良誤認の「対象」から「対象外」へ、組成が変更された場合でも、再印刷対象とする。
     * </pre>
     *
     * @param itemChangeState 品番情報の変更状態
     * @return 判定結果
     *  <pre>
     *  - true : 再印刷対象の更新あり
     *  - false : 再印刷対象の更新なし
     *  </pre>
     */
    private boolean isOrderPrintTargetChanged(final ItemChangeStateModel itemChangeState) {
        // TODO JUNoTで発注書のPDFを作成する場合は、全ての色の組成を印字するため、本判定条件も変更が必要となる。
        return itemChangeState.isCooCodeChanged() || itemChangeState.isPrintCompositionChanged();
    }

    /**
     * 発注書の再印刷対象か判定する.
     *
     * <p>以下の全ての条件に該当する発注情報は、発注書の再印刷対象とする。</p>
     *
     * <pre>
     * - 承認ステータスが、「MD承認済かつ受注確定済(1)」「MD承認差し戻し、受注確定済(2)」「受注確定済、MD未承認(3)」。
     *   TODO 「MD承認差し戻し、受注確定済(2)」「受注確定済、MD未承認(3)」は発注生産に連携済みの状態のため、再印刷対象とする。
     *          また、今後、「受注確認書」を作成する場合も再印刷対象となる。）
     * - 発注書が完納以外。
     *   - 完納の条件：「全済区分」が「済(0)」、または「製品完納区分」が「完納(6)」または「自動完納(5)」。
     * - 発注書に紐付く、納品書の初回納品依頼が「承認済」以外。
     * </pre>
     *
     * @param tOrder DB上の発注情報
     * @return 判定結果
     *  <pre>
     *  - true : 再印刷対象
     *  - false : 再印刷対象外
     *  </pre>
     */
    private boolean isOrderPrintTarget(final TOrderEntity tOrder) {
        return isOrderConfirmed(tOrder.getOrderApproveStatus())
                && !isCompleteOrder(tOrder)
                && !deliveryRepository.existsByOrderIdAndDeliveryApproveStatus(tOrder.getId(), ApprovalType.APPROVAL.getValue());
    }

    /**
     * 発注SKUの製品発注数の合計を算出する.
     *
     * @param orderSkuModelList 発注SKUのリスト
     * @return 製品発注数の合計
     */
    public int sumProductOrderLot(final List<OrderSkuModel> orderSkuModelList) {
        return orderSkuModelList.stream()
                .mapToInt(orderSkuModel -> orderSkuModel.getProductOrderLot())
                .sum();
    }

    /**
     * 予定用尺と実用尺の値セット.
     * 費目=01の場合は、固定値1をセット、費目 != 01の場合は0を設定する
     *
     * @param expenseItem 画面上で入力した費目
     * @return 予定用尺と実用尺の値
     */
    public BigDecimal setValuePlanLengthActualAndNecessaryLengthActual(final ExpenseItemType expenseItem) {
        if (Objects.equals(expenseItem, ExpenseItemType.PRODUCT_ORDER)) {
            return BigDecimal.ONE;
        }
        return BigDecimal.ZERO;
    }

    /**
     * 生産発注日を基にどの締め日に該当するか判定する.
     *
     * 前月締め日 < 生産発注日 <= 当月締め日 → 当月：1を返却
     * 前々月締め日 < 生産発注日 <= 前月締め日 → 前月：2を返却
     * 前々々月締め日 < 生産発注日 <= 前々月締め日 → 前々月：3を返却
     * 上記外 → その他：0を返却
     *
     * @param productOrderAt 生産発注日
     * @param orderSkuEntity 発注SKU情報
     * @return 該当締め日のCONST値
     */
    public int judgeEndAtTypeByProductOrderAt(final Date productOrderAt, final TOrderSkuEntity orderSkuEntity) {
        // 締め日を取得する
        // 当月締め日
        Date monthEndAt = orderSkuEntity.getMonthEndAt();
        // 前月締め日
        Date previousMonthEndAt = orderSkuEntity.getPreviousMonthEndAt();
        // 前々月締め日
        Date monthBeforeEndAt = orderSkuEntity.getMonthBeforeEndAt();
        // 前々々月締め日
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date previousMonthBeforeEndAt = mKanmstComponent.getPreviousMonthBeforeEndAt(sdf.format(monthBeforeEndAt));

        // 生産発注日のフォーマット
        Date formattedProductOrderAt = formatProductOrderAt(productOrderAt);
        // 発注生産日と締め日を比較し、該当締め日を判断する
        if (compareProductOrderAtWithEndAt(formattedProductOrderAt, previousMonthBeforeEndAt, monthBeforeEndAt)) {
            return EndAtTypeConstants.MONTH_BEFORE; // 前々月
        }
        if (compareProductOrderAtWithEndAt(formattedProductOrderAt, monthBeforeEndAt, previousMonthEndAt)) {
            return EndAtTypeConstants.PREVIOUS_MONTH; // 前月
        }
        if (compareProductOrderAtWithEndAt(formattedProductOrderAt, previousMonthEndAt, monthEndAt)) {
            return EndAtTypeConstants.THIS_MONTH; // 当月
        }
        if (monthEndAt.before(productOrderAt)) {
            return EndAtTypeConstants.FUTURE_MONTH; // 未来月
        }

        return EndAtTypeConstants.PAST_MONTH; // 過去月
    }

    /**
     * 生産発注日と締め日を比較する.
     *
     * 前月締め日 < 発注日 <= 当月締め日であればtrueを返却
     *
     * @param productOrderAt 生産発注日
     * @param previousMonthEndAt 前月締め日
     * @param monthEndAt 当月締め日
     * @return true：前月締め日 < 発注日 <= 当月締め日, false : 上記以外
     */
    private boolean compareProductOrderAtWithEndAt(final Date productOrderAt, final Date previousMonthEndAt, final Date monthEndAt) {
        if (productOrderAt.after(previousMonthEndAt)
                && (productOrderAt.before(monthEndAt) || productOrderAt.equals(monthEndAt))) {
            return true;
        }
        return false;
    }

    /**
     * 生産発注日を整形する.
     *
     * @param productOrderAt 整形前の生産発注日
     * @return 整形前後の生産発注日
     */
    private Date formatProductOrderAt(final Date productOrderAt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        LocalDate ld = LocalDate.parse(sdf.format(productOrderAt), DateTimeFormatter.ofPattern("yyyyMMdd"));

        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 品番情報変更に伴う、発注情報の更新を行う.
     *
     * 原産国に変更があれば、発注情報の原産国を再セット
     * 品番に変更があれば、発注情報と発注SKUの品番を再セット
     *
     * <p>「生産メーカー」、「生産工場」、「委託先工場」、「製造担当」については、
     * 品番確定後は更新できないため、発注確定や発注更新時にのみ品番情報で上書きする。
     * 品番確定しないと、発注確定できない（発注生産に連携されない）ため、これらの情報を変更しても、発注生産に連携する必要はない。</p>
     *
     * @param item 更新対象の品番情報
     * @param tOrders DB上の発注情報リスト
     * @param itemChangeState 品番情報の変更状態
     */
    public void updateOrderByItemChange(final ItemModel item, final List<TOrderEntity> tOrders, final ItemChangeStateModel itemChangeState) {
        if (CollectionUtils.isEmpty(tOrders)) {
            // 発注情報がない場合、処理しない
            return;
        }

        if (itemChangeState.isPartNoChanged()) {
            // 品番に変更がある場合

            // 発注情報の品番と発注SKU情報の品番を再セット
            tOrders.stream().forEach(tOrder -> {
                // ※品番確定をしないと、受注確定までステータスが遷移しないため、MD承認後に品番を変更することはできない。
                //   そのため、MD承認済み→未承認に戻す処理は必要ない。
                tOrder.setPartNoId(item.getId());
                tOrder.setPartNo(item.getPartNo());

                // 連携対象の設定
                setLinkingTarget(tOrder);

                // PRD_0142 #10423 JFE add start
                // TAGDAT作成フラグに未作成を設定
                tOrder.setTagdatCreatedFlg(NOT_CREATED);
                // PRD_0142 #10423 JFE add end
            });

            // 発注IDリスト
            final List<BigInteger> orderIds = tOrders.stream().map(tOrder -> tOrder.getId()).collect(Collectors.toList());

            // 発注IDリストを基にDBから最新の発注SKUリストを取得
            final List<TOrderSkuEntity> tOrderSkus = orderSkuRepository.findByOrderIdList(orderIds);

            if (!CollectionUtils.isEmpty(tOrderSkus)) {
                // 発注SKUの品番を再セット
                tOrderSkus.stream().forEach(tOrderSku -> tOrderSku.setPartNo(item.getPartNo()));
            }

            // 発注SKU情報を更新
            orderSkuRepository.saveAll(tOrderSkus);
        }

        if (isOrderPrintTargetChanged(itemChangeState)) {
            // 発注書の再印刷対象の更新がある場合
            resetPrintOrders(tOrders);
        }

        // 発注情報を更新
        orderRepository.saveAll(tOrders);
    }

    /**
     * MD承認済→MD未承認に戻す場合は、承認済みの情報を初期値で設定する。
     *
     * MD承認済の発注情報の発注承認ステータスと連携ステータスを再セット.
     * 発注承認ステータス=3
     * 連携ステータス=0
     *
     * @param tOrder 発注情報
     */
    public void resetApprovedOrderStatus(final TOrderEntity tOrder) {
        // 発注承認ステータスに3を設定
        tOrder.setOrderApproveStatus(OrderApprovalType.CONFIRM.getValue());
        // 発注承認日を空にする
        tOrder.setOrderApproveAt(null);
        // 裁断回数に0を設定
        tOrder.setCutCount(0);
        // 裁断完納区分に0を設定
        tOrder.setCutCompleteOrderType(CompleteOrderType.INCOMPLETE);
        // 裁断済区分に9を設定
        tOrder.setCutCompleteType(CompleteType.INCOMPLETE);
        // 最新裁断日に空を設定
        tOrder.setCurrentCutAt(null);
        // 最新納品依頼日に空を設定
        tOrder.setCurrentDeliveryRequestAt(null);
    }

    /**
     * 連携対象の設定.
     *
     * @param tOrder 発注情報
     */
    public void setLinkingTarget(final TOrderEntity tOrder) {
        // 発注書出力フラグに0を設定
        tOrder.setOrderSheetOut(OrderSheetOutType.NOT_ISSUE);

        // 製品最終処理日を設定
        setProductLastDisposalAt(tOrder);

        // SQ送信区分を設定
        setSqSendType(tOrder);

        // 連携ステータスに0を設定
        tOrder.setLinkingStatus(LinkingStatusType.TARGET);
    }

    /**
     * 製品最終処理日の設定.
     *
     * <p>製品最終処理日がNULL以外の場合、製品最終処理日に更新日付（時分秒を除く）を設定する。</p>
     *
     * @param tOrder 発注情報
     */
    public void setProductLastDisposalAt(final TOrderEntity tOrder) {
        if (Objects.nonNull(tOrder.getProductLastDisposalAt())) {
            // 製品最終処理日がNULL以外の場合
            // 製品最終処理日に更新日付（時分秒を除く）を設定
            tOrder.setProductLastDisposalAt(DateUtils.truncateDate(new Date()));
        }
    }

    /**
     * SQ送信区分の設定.
     *
     * <p>MD承認済かつ受注確定済の場合、SQ送信区分に1(SQ送信対象)を設定する。</p>
     * <p>未承認の場合、SQ送信区分に0を設定する。</p>
     *
     * @param tOrder 発注情報
     */
    public void setSqSendType(final TOrderEntity tOrder) {
        if (isOrderApproved(tOrder)) {
            // MD承認済かつ受注確定済の場合、
            // SQ送信区分に1(SQ送信対象)を設定
            tOrder.setSqSendType(SendType.SEND_TARGET);
        } else {
            // 未承認の場合、
            // SQ送信区分に0を設定
            tOrder.setSqSendType(SendType.SEND);
        }
    }

    /**
     * 発注書を再印刷対象に変更する.
     *
     * @param tOrders DB上の発注情報リスト
     */
    public void resetPrintOrders(final List<TOrderEntity> tOrders) {
        if (CollectionUtils.isEmpty(tOrders)) {
            // 発注情報がない場合、処理しない
            return;
        }

        tOrders.stream()
                .filter(tOrder -> isOrderPrintTarget(tOrder))
                .forEach(tOrder -> {
                    setLinkingTarget(tOrder); // 連携ステータスの更新
                    printOrder(tOrder); // 発注書発行処理
                });
    }

    /**
     * 発注書送付処理.
     *
     * @param tOrder 発注情報
     */
    public void printOrder(final TOrderEntity tOrder) {
        // 発注情報を元に印刷用の情報を取得する。
        final ExtendedTOrderEntity extendedTOrder = extendedTOrderRepository.findById(tOrder.getId()).orElse(new ExtendedTOrderEntity());
        final ExtendedTItemEntity extendedTItem = extendedTItemRepository.findById(tOrder.getPartNoId()).orElse(new ExtendedTItemEntity());
        final CustomLoginUser loginUser = loginUserComponent.getLoginUser();

        final OrderApprovalType orderApprovalType = OrderApprovalType.convertToType(extendedTOrder.getOrderApproveStatus());
        if (orderApprovalType == OrderApprovalType.REJECT || orderApprovalType == OrderApprovalType.CONFIRM) {
            // 受注確定済み、かつ、発注未承認 の場合、

            // 受注確定情報（即時）を、メール送信キューに格納する
            stackTOrderRequestSendMailComponent.saveOrderSendMailData(tOrder, extendedTItem, loginUser);
        } else if (orderApprovalType == OrderApprovalType.APPROVED) {
            // 発注承認済みの場合

            // 発注書[即時] 発行＆メール通知
            stackTOrderSendMailComponent.saveOrderSendMailData(extendedTOrder, extendedTItem, loginUser);
            // 発注承認正式メール送信管理に情報を登録
            stackTOrderOfficialSendMailComponent.saveOrderOfficialSendMailData(extendedTOrder, extendedTItem, loginUser);
        }
    }

    /**
     * 最新の発注情報で他テーブルの更新.
     *
     * @param tOrderEntity 最新の発注情報(DB登録値)
     * @param tItemEntity 最新の品番情報(DB登録値)
     * @param loginUser ログインユーザー
     */
    public void updateOtherTable(
            final TOrderEntity tOrderEntity,
            final TItemEntity tItemEntity,
            final CustomLoginUser loginUser) {
        final BigInteger productOrderSupplierId = findOrderSupplierId(tOrderEntity);
        final OrderChangeStateModel changeState = generateItemChangeState(tItemEntity, tOrderEntity, productOrderSupplierId);

        // 品番情報更新
        updateItem(tItemEntity, tOrderEntity, changeState, productOrderSupplierId);

        // 優良誤認承認情報更新
        updateMisleadingRepresentation(tItemEntity, tOrderEntity, changeState, loginUser);
    }

    /**
     * @param order 発注情報
     * @return 発注先メーカー情報Id
     */
    private BigInteger findOrderSupplierId(final TOrderEntity order) {
        return tOrderSupplierRepository.findIdByCurrentOrderInfo(
                order.getPartNoId(),
                order.getMdfMakerCode(),
                OrderCategoryType.PRODUCT);
    }

    /**
     * @param tItemEntity 品番情報
     * @param tOrderEntity 発注情報
     * @param productOrderSupplierId 発注先メーカーID(最新製品)
     * @return 変更状態
     */
    private OrderChangeStateModel generateItemChangeState(
            final TItemEntity tItemEntity,
            final TOrderEntity tOrderEntity,
            final BigInteger productOrderSupplierId) {
        final OrderChangeStateModel itemChangeState = new OrderChangeStateModel();

        // 原産国変更あり
        final boolean cooCodeChanged = !Objects.equals(tItemEntity.getCooCode(), tOrderEntity.getCooCode());
        itemChangeState.setCooCodeChanged(cooCodeChanged);

        // 発注先メーカーID変更あり
        final boolean mdfMakerCodeChanged = !Objects.equals(tItemEntity.getCurrentProductOrderSupplierId(), productOrderSupplierId);
        itemChangeState.setMdfMakerCodeChanged(mdfMakerCodeChanged);

        return itemChangeState;
    }

    /**
     * 最新の発注情報で品番情報を更新する.
     *
     * @param tItemEntity 最新の品番情報(DB登録値)
     * @param tOrderEntity 最新の発注情報(DB登録値)
     * @param productOrderSupplierId 発注先メーカーID(最新製品)
     * @param changeState 変更状態
     */
    private void updateItem(
            final TItemEntity tItemEntity,
            final TOrderEntity tOrderEntity,
            final OrderChangeStateModel changeState,
            final BigInteger productOrderSupplierId) {
        setValueForItemEntity(tItemEntity, tOrderEntity, changeState, productOrderSupplierId);
        itemRepository.save(tItemEntity);
    }

    /**
     * 最新の発注情報で品番情報の項目を設定する.
     *
     * @param tItemEntity 最新の品番情報(DB登録値)
     * @param tOrderEntity 最新の発注情報(DB登録値)
     * @param productOrderSupplierId 発注先メーカーID(最新製品)
     * @param changeState 変更状態
     */
    private void setValueForItemEntity(
            final TItemEntity tItemEntity,
            final TOrderEntity tOrderEntity,
            final OrderChangeStateModel changeState,
            final BigInteger productOrderSupplierId) {
        // 発注承認済(発注承認時または発注承認後の訂正時)の以外の場合は更新しない。
        if (!OrderApprovalType.APPROVED.getValue().equals(tOrderEntity.getOrderApproveStatus())) {
            return;
        }

        // 原産国変更あり
        if (changeState.isCooCodeChanged()) {
            final String cooCode = tOrderEntity.getCooCode();
            // 原産国
            tItemEntity.setCooCode(cooCode);
            // 優良誤認承認区分（国）
            final QualityApprovalType status = misleadingRepresentationComponent.decideQualityCooStatus(cooCode);
            tItemEntity.setQualityCooStatus(status.getValue());
        }

        // 発注先メーカーID変更あり
        if (changeState.isMdfMakerCodeChanged()) {
            // 発注先メーカーID(最新製品)
            tItemEntity.setCurrentProductOrderSupplierId(productOrderSupplierId);
            // 優良誤認承認区分（有害物質）
            final QualityApprovalType status = misleadingRepresentationComponent.decideQualityHarmfulStatus(tOrderEntity.getMdfMakerCode());
            tItemEntity.setQualityHarmfulStatus(status.getValue());
        }

        // 原産国または発注先メーカーIDに変更がある場合は優良誤認区分再セット
        if (changeState.isCooCodeChanged() || changeState.isMdfMakerCodeChanged()) {
            tItemEntity.setMisleadingRepresentation(misleadingRepresentationComponent.isMisleadingRepresentationTarget(tItemEntity));
        }

        // 製造担当を再セット
        tItemEntity.setMdfStaffCode(tOrderEntity.getMdfStaffCode());

        // 上代をセット
        tItemEntity.setRetailPrice(tOrderEntity.getRetailPrice());
        // その他原価をセット
        tItemEntity.setOtherCost(tOrderEntity.getOtherCost());
        // 連携ステータスを連携対象に更新
        tItemEntity.setLinkingStatus(LinkingStatusType.TARGET);
//      PRD_0163 JFE mod start
//      PRD_0142 #10423 JFE add start
        // TAGDAT作成フラグに未作成を設定
        //tOrderEntity.setTagdatCreatedFlg(NOT_CREATED);
        tItemEntity.setTagdatCreatedFlg(NOT_CREATED);
//      PRD_0142 #10423 JFE add end
//      PRD_0163 JFE mod end

//      PRD_0206 && TEAM_ALBUS-16 add start
        // 生地原価
        tItemEntity.setMatlCost(tOrderEntity.getMatlCost());
        // 加工原価
        tItemEntity.setProcessingCost(tOrderEntity.getProcessingCost());
        // 附属原価
        tItemEntity.setAccessoriesCost(tOrderEntity.getAttachedCost());
//      PRD_0206 && TEAM_ALBUS-16 add end

    }

    /**
     * メーカーか原産国の変更がある場合、優良誤認承認情報更新.
     *
     * @param tItemEntity 品番情報
     * @param tOrderEntity 発注情報
     * @param changeState 変更状態
     * @param customLoginUser ログインユーザ情報
     */
    private void updateMisleadingRepresentation(
            final TItemEntity tItemEntity,
            final TOrderEntity tOrderEntity,
            final OrderChangeStateModel changeState,
            final CustomLoginUser customLoginUser) {
        if (!isOrderApproved(tOrderEntity)) {
            return; // 発注未承認の場合は更新しない
        }

        if (!changeState.isCooCodeChanged()
                && !changeState.isMdfMakerCodeChanged()) {
            return; // メーカーか原産国の変更がなければ更新しない
        }

        final ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(tItemEntity, itemModel);

        final List<OrderSupplierModel> orderSuppliers = new ArrayList<>(1);
        final OrderSupplierModel orderSupplier = new OrderSupplierModel();
        orderSupplier.setSupplierCode(tOrderEntity.getMdfMakerCode());
        orderSuppliers.add(orderSupplier);
        itemModel.setOrderSuppliers(orderSuppliers);

        final ItemChangeStateModel itemChangeState = new ItemChangeStateModel();
        itemChangeState.setCooCodeChanged(changeState.isCooCodeChanged());
        itemChangeState.setMdfMakerCodeChanged(changeState.isMdfMakerCodeChanged());

        misleadingRepresentationComponent.upsertMisleadingRepresentation(itemModel, itemChangeState, customLoginUser);
    }

    /**
     * 読み取り専用の発注の場合、更新不可エラーとする.
     *
     * @param expenseItemType DBのExpenseItemType
     */
    public void validateReadOnly(final ExpenseItemType expenseItemType) {
        // readOnlyの場合、エラー
        if (isReadOnly(expenseItemType)) {
            throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_012));
        }
    }

    // PRD_0144 #10776 mod JFE start
    /**
     * 発注情報の費目が「01 製品発注」か「04 縫製発注」以外の場合、読み取り専用の発注とする.
     *
     * @param expenseItemType DBのExpenseItemType
     * @return true: 読み取り専用 / false: 更新可能
     */
    public boolean isReadOnly(final ExpenseItemType expenseItemType) {
       // return ExpenseItemType.PRODUCT_ORDER != expenseItemType;
    	return (ExpenseItemType.PRODUCT_ORDER != expenseItemType && ExpenseItemType.SEWING_ORDER != expenseItemType);
    }
    // PRD_0144 #10776 mod JFE end
}
