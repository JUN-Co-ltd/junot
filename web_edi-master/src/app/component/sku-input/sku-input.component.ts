import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormBuilder, FormArray, Validators } from '@angular/forms';

import { Path, AuthType } from '../../const/const';

import { JunpcSizmstSearchCondition } from '../../model/junpc-sizmst-search-condition';
import { OrderSku } from '../../model/order-sku';
import { Sku } from '../../model/sku';
import { JunpcSizmst } from '../../model/junpc-sizmst';

import { JunpcSizmstService } from '../../service/junpc-sizmst.service';
import { OrderService } from '../../service/order.service';
import { Delivery } from 'src/app/model/delivery';

@Component({
  selector: 'app-sku-input',
  templateUrl: './sku-input.component.html',
  styleUrls: ['./sku-input.component.scss']
})
export class SkuInputComponent implements OnInit {

  @Input() private skuList: Sku[] = []; // SKUリスト
  @Input() private orderSkuValue: OrderSku[] = [];  // 発注SKUリスト
  @Input() deliveryList: Delivery[] = []; // 親画面から受け取った納品依頼リスト
  @Input() isConfirmed = false;   // 親画面から受け取った発注確定済み判断フラグ
  @Input() isOrderComplete = false;   // 親画面から受け取った納品依頼済または完納済判断フラグ
  @Input() parentForm: FormGroup; // 親画面のForm
  @Input() submitted = false;     // 親画面でsubmitしたか
  @Input() path = '';             // 親画面から受け取ったURLパス(new,view,edit,delete)
  @Input() affiliation: AuthType; // 親画面から受け取ったユーザー権限

  noSizeMstMsg: string = null;  // サイズマスタ取得できない時のメッセージ

  sizeMasterList: JunpcSizmst[] = []; // サイズマスタリスト

  constructor(
    private formBuilder: FormBuilder,
    private junpcSizmstService: JunpcSizmstService,
    private orderService: OrderService
  ) { }

  ngOnInit() {
    const partNo = this.parentForm.get('partNo').value.slice(0, 3) as string;
    // 品番でサイズマスタ取得
    this.junpcSizmstService.getSizmst({ hscd: partNo } as JunpcSizmstSearchCondition).subscribe(
      genericList => {
        this.sizeMasterList = genericList.items;
        if (this.sizeMasterList == null || this.sizeMasterList.length === 0) {
          // マスタが取得できなかった
          this.noSizeMstMsg = 'サイズが設定されていません';
          return;
        }
        this.noSizeMstMsg = null;
        if (this.skuList.length === 0) { return; }
        // 親からSKUリストが渡されている場合は値設定。
        const viewList = this.prepareSkusListForView(this.skuList);
        this.createSetValueForm(viewList, this.parentForm.controls.partNo.value, this.sizeMasterList, this.orderSkuValue);
      });
  }

  /**
   * @return this.parentForm.get('orderSkus') as FormArray
   */
  private get orderSkuFormArray(): FormArray {
    return this.parentForm.get('orderSkus') as FormArray;
  }


  /**
   * @return this.parentForm.get('orderSkus').controls
   */
  get orderSkuFormArrayControl(): any {
    return this.orderSkuFormArray.controls;
  }


  /**
   * サイズのフォームを作成して返す.
   * @param color カラーコード
   * @param size サイズ
   * @param partNo 品番
   * @return サイズのフォーム
   */
  private createSizeFormGroup(color: string, size: string, partNo: string): FormGroup {
    return this.formBuilder.group({
      id: [null],
      colorCode: [color],
      size: [size],
      productOrderLot: [null, [Validators.pattern(/^[0-9,]*$/)]],
      partNo: [partNo]
    });
  }

  /**
   * 指定したカラーの数量を合計して返す.
   * @param colorCode カラーコード
   * @return 指定したカラーの数量合計
   */
  sumQuantityByColor(colorCode: string): number {
    // 色コード別の入力情報を取得する
    let colorQuantity = 0;
    const orderSkus = this.orderSkuFormArray.getRawValue();
    if (orderSkus.length === 0) { return colorQuantity; }

    orderSkus.some(orderSku => {
      if (orderSku.colorCode === colorCode) {
        orderSku.sizeList.forEach(sku => colorQuantity += Number(sku.productOrderLot ? sku.productOrderLot : 0));
        return true;
      }
    });
    return colorQuantity;
  }

  /**
   * 指定したサイズの数量を合計して返す.
   * @param size 集計を行いたいサイズ
   * @return 指定したサイズの数量合計
   */
  sumQuantityBySize(size: string): number {
    let qantity = 0;
    const orderSkus = this.orderSkuFormArray.getRawValue();
    if (orderSkus.length === 0) { return qantity; }

    orderSkus.forEach(colorSkus =>
      colorSkus.sizeList.forEach(sku => {
        if (sku.size === size) {
          qantity += Number(sku.productOrderLot ? sku.productOrderLot : 0);
        }
      })
    );
    return qantity;
  }

  /**
   * 全数量を合計して返す.
   * @returns 全数量の数量合計
   */
  sumAllQuantity(): number {
    const orderSkus = this.orderService.convertSku(this.orderSkuFormArray.getRawValue());
    return this.orderService.sumAllOrderQuantity(orderSkus);
  }

  /**
   * データがあった時の表示処理
   * @param viewskuList 発注SKUリスト
   * @param partNo 品番
   * @param sizeMasterList サイズマスタリスト
   * @param orderSkuValue 発注SKU
   */
  private createSetValueForm(viewskuList: { colorCode: string, colorName: string, skus: Sku[] }[], partNo: string,
    sizeMasterList: JunpcSizmst[], orderSkuValue: OrderSku[]): void {
      viewskuList.forEach(viewsku => {
      this.orderSkuFormArray.push(this.formBuilder.group({
        colorCode: viewsku.colorCode,
        colorName: viewsku.colorName,
        colorQuantity: '',
        sizeList: this.generateSkuFormArray(viewsku.colorCode, viewsku.skus, partNo, sizeMasterList, orderSkuValue)
      }));
    });
  }

  /**
   * SKUFormArrayを作成する.
   * @param colorCode　カラーコード
   * @param sizeListValue SKUリスト
   * @param partNo 品番
   * @param sizeMasterList サイズマスタリスト
   * @param orderSkuValue 発注SKU
   * @returns FormArray
   */
  private generateSkuFormArray(colorCode: string, skuList: Sku[], partNo: string,
    sizeMasterList: JunpcSizmst[], orderSkuValue: OrderSku[]): FormArray {
    const sizeList = this.formBuilder.array([]);
    sizeMasterList.forEach(sizeMaster => {
      const sizeFromGroup = this.createSizeFormGroup(colorCode, sizeMaster.szkg, partNo);
      const isViewSizeExits = skuList.some(sku => {
        if (sku.size === sizeMaster.szkg) {
          if (orderSkuValue != null && orderSkuValue.length > 0) {
            orderSkuValue.some(orderSku => {
              if (orderSku.colorCode === colorCode && orderSku.size === sku.size) {
                sizeFromGroup.controls.id.patchValue(orderSku.id);
                sizeFromGroup.controls.productOrderLot.patchValue(orderSku.productOrderLot >= 0 ? orderSku.productOrderLot : null);
                return true;
              }
            });
          }
          return true;
        }
      });
      // サイズに該当する発注SKUが無い
      // または パスがVIEW
      // または パスがAPPROVAL
      // または 納品依頼がある または 完納
      if (!isViewSizeExits || this.path === Path.VIEW || this.path === Path.APPROVAL || this.isOrderComplete ) {
        sizeFromGroup.controls.productOrderLot.disable();
      }
      sizeList.push(sizeFromGroup);
    });
    return sizeList;
  }

  /**
   * SKUリストからカラーコード、カラー名をキーにしたSKUリストを作成して返す.
   * @param skuList SKUリスト
   * @returns 画面表示用SKUリスト
   */
  private prepareSkusListForView(skuList: Sku[]): { colorCode: string, colorName: string, skus: Sku[] }[] {
    const colorList: Sku[] = [];
    const viewSkus: { colorCode: string, colorName: string, skus: Sku[] }[] = [];
    const keyOrder: Set<string> = new Set();

    skuList.forEach(sku => {
      colorList[sku.colorCode] = sku;
      keyOrder.add(sku.colorCode);
    });

    keyOrder.forEach(key => {
      const color = {
        'colorCode': colorList[key].colorCode as string,
        'colorName': colorList[key].colorName as string,
        'skus': [] as Sku[]
      };
      color.skus = skuList.filter(sku => sku.colorCode === key);
      viewSkus.push(color);
    });

    return viewSkus;
  }
}
