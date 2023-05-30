import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { DeliveryDistributionSpecificationType, ValidatorsPattern } from '../../const/const';
import { DistributionValueValidator } from './validator/delivery-distribution-modal-validator.directive';

class ResultItem {
  distributionType: number;
  specifiedValue: number;
}

@Component({
  selector: 'app-delivery-distribution-modal',
  templateUrl: './delivery-distribution-modal.component.html',
  styleUrls: ['./delivery-distribution-modal.component.scss']
})
export class DeliveryDistributionModalComponent implements OnInit {
  /** htmlから参照したい定数を定義 */
  readonly DELIVERY_DISTRIBUTION_SPECIFICATION_TYPE = DeliveryDistributionSpecificationType;

  @Input() totalDistributionValue: number;

  /** フォーム */
  distributionForm: FormGroup;
  /** 結果格納 */
  resultItem: ResultItem[] = [];

  constructor(
    public activeModal: NgbActiveModal,
    private formBuilder: FormBuilder
  ) { }

  ngOnInit() {
    // FormGroupを作成する
    this.distributionForm = this.createForm();
    // 初期値設定
    this.patchValuesToForm();
    // 初期値にあわせて配分数の入力欄を非活性にする
    this.distributionForm.controls.distributionNumberValue.disable();
  }

  /**
   * FormGroupを返す
   */
  createForm(): FormGroup {
    return this.formBuilder.group({
      totalDistributionValue: [null],
      distributionType: [null],
      distributionRateValue:
        [null, { validators: [DistributionValueValidator, Validators.pattern(ValidatorsPattern.NUMERIC_0_9)], updateOn: 'blur' }],
      distributionNumberValue:
        [null, { validators: [DistributionValueValidator, Validators.pattern(ValidatorsPattern.NUMERIC_0_9)], updateOn: 'blur' }]
    });
  }

  /**
   * 初期値をformに設定する
   */
  private patchValuesToForm(): void {
    this.distributionForm.patchValue({ totalDistributionValue: this.totalDistributionValue });
    this.distributionForm.patchValue({ distributionType: DeliveryDistributionSpecificationType.DISTRIBUTION_RATE });  // 初期値は配分率指定
  }

  /**
   * ラジオボタン変更時の処理
   * @param distributionType 配分指定種別
   */
  onChangeDistributionType(distributionType: DeliveryDistributionSpecificationType): void {
    switch (distributionType) {
      case DeliveryDistributionSpecificationType.DISTRIBUTION_RATE:          // 配分率に変更
        this.distributionForm.patchValue({ distributionNumberValue: null }); // 配分数の入力欄を空にする
        this.distributionForm.controls.distributionRateValue.enable();       // 配分率の入力欄を活性にする
        this.distributionForm.controls.distributionNumberValue.disable();    // 配分数の入力欄を非活性にする
        break;
      case DeliveryDistributionSpecificationType.DISTRIBUTION_NUMBER:        // 配分数に変更
        this.distributionForm.patchValue({ distributionRateValue: null });   // 配分率の入力欄を空にする
        this.distributionForm.controls.distributionRateValue.disable();      // 配分率の入力欄を非活性にする
        this.distributionForm.controls.distributionNumberValue.enable();     // 配分数の入力欄を活性にする
        break;
    }
  }

  /**
   * 選択ボタンクリック時、親ページに入力したデータを渡して、モーダルを閉じます。
   */
  onSelect(): void {
    if (this.distributionForm.getRawValue()['distributionType'] === DeliveryDistributionSpecificationType.DISTRIBUTION_RATE) {
      // 配分率を指定
      this.resultItem.push({
        distributionType: this.distributionForm.getRawValue()['distributionType'],
        specifiedValue: this.distributionForm.getRawValue()['distributionRateValue']
      } as ResultItem);
    } else {
      // 配分数を指定
      this.resultItem.push({
        distributionType: this.distributionForm.getRawValue()['distributionType'],
        specifiedValue: this.distributionForm.getRawValue()['distributionNumberValue']
      } as ResultItem);
    }

    const result = this.resultItem[0];
    this.activeModal.close(result);
  }

  /**
   * フォームを取得する。
   * @return mainForm.controls
   */
  get f(): any { return this.distributionForm.controls; }

  /**
   * エラー表示の有無を返却する。
   * @param value FormControl
   */
  isErrorDisplay(value: FormControl): boolean {
    return value.invalid && (value.dirty || value.touched);
  }
}
