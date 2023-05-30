import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, FormArray, Validators } from '@angular/forms';

import { Path, ValidatorsPattern } from '../../const/const';
import { ListUtils } from '../../util/list-utils';

import { FukukitaruOrderSku } from '../../model/fukukitaru-order-sku';
import { ScreenSettingFukukitaruSku } from '../../model/screen-setting-fukukitaru-sku';
import { OrderSkuValue } from 'src/app/interface/order-sku-value';

@Component({
  selector: 'app-fukukitaru-sku-input',
  templateUrl: './fukukitaru-sku-input.component.html',
  styleUrls: ['./fukukitaru-sku-input.component.scss']
})
export class FukukitaruSkuInputComponent implements OnInit {
  @Input() private skuList: ScreenSettingFukukitaruSku[] = []; // フクキタル画面構成用SKUリスト
  @Input() private orderSkuValue: FukukitaruOrderSku[] = [];  // フクキタル発注SKUリスト
  @Input() parentForm: FormGroup; // 親画面のForm
  @Input() submitted = false;     // 親画面でsubmitしたか
  @Input() path = '';             // 親画面から受け取ったURLパス(new,view,edit)
  @Output() changeProductOrderLot = new EventEmitter();     // 入力値変更時のEventEmitter

  noSizeMstMsg: string = null;  // サイズマスタ取得できない時のメッセージ
  sizeMasterList: string[] = []; // サイズマスタリスト

  constructor(
    private formBuilder: FormBuilder
  ) { }

  ngOnInit() {
    if (ListUtils.isEmpty(this.skuList)) {
      return;
    }

    this.sizeMasterList = this.genarateSizeList();
    const viewList = this.prepareSkusListForView(this.skuList);
    this.createSetValueForm(viewList, this.sizeMasterList, this.orderSkuValue);
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
  private createSizeFormGroup(color: string, size: string): FormGroup {
    return this.formBuilder.group({
      id: [null],
      colorCode: [color],
      size: [size],
      productOrderLot: [null, [Validators.pattern(new RegExp(ValidatorsPattern.POSITIVE_INTEGER))]]
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
    const orderSkus = this.convertSku(this.orderSkuFormArray.getRawValue());
    return this.sumAllOrderQuantity(orderSkus);
  }

  /**
   * 製品発注数を合計する.
   * @returns 製品発注数合計
   */
  private sumAllOrderQuantity(orderSkus: OrderSkuValue[]): number {
    let qantity = 0;
    if (orderSkus === undefined || orderSkus.length === 0) { return qantity; }
    orderSkus.forEach(sku => qantity += Number(sku.productOrderLot ? sku.productOrderLot  : 0));
    return qantity;
  }

  /**
   * formのvalueからorderSkuをすべて取得する。
   *
   * @param orderSkus 色別に分類されたorderSkuのリスト
   * @returns orderSkuリスト
   */
  private convertSku(orderSkus: any): OrderSkuValue[] {
    const reqColorList: OrderSkuValue[] = [];
    const colorList = orderSkus.filter(sku => sku.colorCode !== '');
    colorList.forEach(sku => {
      // 製品発注数がNullのデータは除外する。
      sku.sizeList = sku.sizeList.filter(size => size.productOrderLot > 0);
      Array.prototype.push.apply(reqColorList, sku.sizeList);
    });
    return reqColorList;
  }

  /**
   * データがあった時の表示処理
   * @param viewskuList 発注SKUリスト
   * @param sizeMasterList サイズマスタリスト
   * @param orderSkuValue 発注SKU
   */
  private createSetValueForm(viewskuList: { colorCode: string, colorName: string, skus: ScreenSettingFukukitaruSku[] }[],
    sizeMasterList: string[], orderSkuValue: FukukitaruOrderSku[]): void {
    viewskuList.forEach(viewsku => {
      this.orderSkuFormArray.push(this.formBuilder.group({
        colorCode: viewsku.colorCode,
        colorName: viewsku.colorName,
        colorQuantity: '',
        sizeList: this.generateSkuFormArray(viewsku.colorCode, viewsku.skus, sizeMasterList, orderSkuValue)
      }));
    });
  }

  /**
   * SKUFormArrayを作成する.
   * @param colorCode　カラーコード
   * @param sizeListValue SKUリスト
   * @param sizeMasterList サイズマスタリスト
   * @param orderSkuValue 発注SKU
   * @returns FormArray
   */
  private generateSkuFormArray(colorCode: string, skuList: ScreenSettingFukukitaruSku[],
    sizeMasterList: string[], orderSkuValue: FukukitaruOrderSku[]): FormArray {
    // 戻り値
    const sizeList = this.formBuilder.array([]);
    // サイズ、カラーごとにFormGroupを生成する
    sizeMasterList.forEach(size => {
      const sizeFromGroup = this.createSizeFormGroup(colorCode, size);

      // 活性/非活性を設定する
      skuList.some(sku => { // 条件に一致するものがあれば、処理を中断させるため、some()を利用
        if (sku.size === size && sku.colorCode === colorCode) {
          if (!sku.enabled) {
            // 非活性
            sizeFromGroup.controls.productOrderLot.disable();
          }
          return true;
        }
      });
      // 発注SKUから発注数を設定する
      orderSkuValue.some(orderSku => { // 条件に一致するものがあれば、処理を中断させるため、some()を利用
        if (orderSku.colorCode === colorCode && orderSku.size === size) {
          sizeFromGroup.controls.id.patchValue(orderSku.id);
          sizeFromGroup.controls.productOrderLot.patchValue(orderSku.orderLot >= 0 ? orderSku.orderLot : null);
          return true;
        }
      });

      sizeList.push(sizeFromGroup);
    });
    return sizeList;
  }

  /**
   * SKUリストからカラーコード、カラー名をキーにしたSKUリストを作成して返す.
   * @param skuList SKUリスト
   * @returns 画面表示用SKUリスト
   */
  private prepareSkusListForView(skuList: ScreenSettingFukukitaruSku[]): {
    colorCode: string, colorName: string, skus: ScreenSettingFukukitaruSku[]
  }[] {
    // 戻り値
    const viewSkus: { colorCode: string, colorName: string, skus: ScreenSettingFukukitaruSku[] }[] = [];

    // skuListから重複しないカラーコードのリストを取得する
    skuList
      .filter((sku1, index, self) => self.findIndex(sku2 => sku2.colorCode === sku1.colorCode) === index)
      .map(sku1 => sku1.colorCode)
      .forEach(colorCode => {
        // カラーコードごとにScreenSettingFukukitaruSkuを収集する
        const viewSku: ScreenSettingFukukitaruSku[] = skuList.filter(sku => {
          return sku.colorCode === colorCode;
        });
        // カラーコード、カラー名をキーにしたSKUリストを作成
        viewSkus.push({
          'colorCode': viewSku[0].colorCode as string,
          'colorName': viewSku[0].colorName as string,
          'skus': viewSku
        });
      });
    return viewSkus;
  }

  /**
   * 画面構成用SKUリストから、サイズの配列を生成する.
   * 重複するサイズは、除去する.
   */
  private genarateSizeList(): string[] {
    return this.skuList
      .filter((sku1, index, self) => self.findIndex(sku2 => sku2.size === sku1.size) === index)
      .map(sku1 => sku1.size);
  }

  /**
   * 入力値変更時の処理.
   */
  onChangeProductOrderLot(): void {
    this.changeProductOrderLot.emit();
  }

}
