import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators, ValidatorFn } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, combineLatest, Subscription } from 'rxjs';
import { tap, flatMap, filter, catchError, finalize } from 'rxjs/operators';

import {
  SubmitType, Path, PreEventParam, SupplierType, AuthType, RegistStatus,
  ValidatorsPattern, KbnMaster, recKbnType, RecKbnDictionary
} from 'src/app/const/const';
import { MaintSire } from 'src/app/model/maint/maint-sire';
import { HeaderService } from 'src/app/service/header.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { MaintSireService } from 'src/app/service//maint/maint-sire.service';
import { NumberUtils } from 'src/app/util/number-utils';
import { FormUtils } from 'src/app/util/form-utils';
import { StringUtils } from 'src/app/util/string-utils';
import { DateUtils } from 'src/app/util/date-utils';

import { BrandCode } from 'src/app/model/brand-code';
import { GenericList } from 'src/app/model/generic-list';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { BrandCodesSearchConditions } from 'src/app/model/brand-codes-search-conditions';

interface Kbn {
  value: string;
  label: string;
  checked: boolean;
}

@Component({
  selector: 'app-maint-sire',
  templateUrl: './maint-sire.component.html',
  styleUrls: ['./maint-sire.component.scss']
})
export class MaintSireComponent implements OnInit, OnDestroy {
  readonly SUBMIT_TYPE = SubmitType;
  readonly AUTH_SUPPLIERS = AuthType.AUTH_SUPPLIERS;
  readonly SUPPLIER_TYPE = SupplierType;
  readonly REGIST_STATUS = RegistStatus;
  readonly PATH = Path;

  /** 区分リスト(画面入力値保持用) */
  reckbnList: Kbn[] = [
    {
      value: recKbnType.SIRE,
      label: RecKbnDictionary[recKbnType.SIRE],
      checked: false
    },
    {
      value: recKbnType.KOJO,
      label: RecKbnDictionary[recKbnType.KOJO],
      checked: false
    },
    {
      value: recKbnType.SPOT,
      label: RecKbnDictionary[recKbnType.SPOT],
      checked: false
    }
  ];

  /** 国内・国外リスト(画面入力値保持用) */
  inOutList: Kbn[] = [
    {
      value: '0',
      label: '国内',
      checked: false
    },
    {
      value: '1',
      label: '国外',
      checked: false
    }
  ];

  /** 生地メーカー(画面入力値保持用) */
  hkijiList: Kbn[] = [
    {
      value: '1',
      label: '生地メーカー',
      checked: false
    }
  ];
  /** 製品／縫製メーカー(画面入力値保持用) */
  hseihinList: Kbn[] = [
    {
      value: '1',
      label: '製品／縫製メーカー',
      checked: false
    }
  ];
  /** 値札発注先(画面入力値保持用) */
  hnefudaList: Kbn[] = [
    {
      value: '1',
      label: '値札発注先',
      checked: false
    }
  ];
  /** 附属品メーカー(画面入力値保持用) */
  hfuzokuList: Kbn[] = [
    {
      value: '1',
      label: '附属品メーカー',
      checked: false
    }
  ];

  /** 画面を非表示にする */
  invisibled = true;
  /** 画面を非活性にする */
  disabled = true;
  /** フォーム */
  mainForm: FormGroup;
  /** タイトル */
  title: string;
  /** パス（画面の表示モード） */
  path = '';
  /** 画面に表示するメッセージ */
  message = {
    /** フッター */
    footer: {
      /** 正常系 */
      success: { code: '', param: null },
      /** 異常系 */
      error: { code: '', param: null }
    }
  };

  /** 仕入先区分リスト */
  sirkbnList: { kbn: string, value: string }[] = [];
  /** 仕入先区分リスト */
  yugaikbnList: { kbn: string, value: string }[] = [];
  /** 送付区分リスト */
  sofkbnList: { kbn: string, value: string }[] = [];
  /** ブランドリスト */
  brandList = [];
  /** ブランドマスタリスト */
  brandmstList: BrandCode[] = [];

  /** 管轄仕入先を非活性にする */
  knktSireDisabled = true;

  /** ローディングサブスクリプション */
  private loadingSubscription: Subscription;



  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private maintSireService: MaintSireService,
    private junpcCodmstService: JunpcCodmstService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.sirkbnList = KbnMaster.sirkbn.map(sirkbn => ({ kbn: sirkbn.kbn, value: sirkbn.value }));
    this.yugaikbnList = KbnMaster.yugaikbn.map(yugaikbn => ({ kbn: yugaikbn.kbn, value: yugaikbn.value }));
    this.sofkbnList = KbnMaster.sofkbn.map(sofkbn => ({ kbn: sofkbn.kbn, value: sofkbn.value }));
    this.getBrand().subscribe();

    // ローディングクリア（親画面のみ）
    this.loadingService.clear();

    // ローディングサブスクリプション開始
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.disabled = isLoading);

    // URLとクエリパラメータの変更を検知
    combineLatest([
      this.route.paramMap,
      this.route.queryParamMap
    ]).subscribe(([paramMap, queryParamMap]) => {
      // URLの最後のパス（画面の表示モード）を取得
      this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
      // URLから仕入先コードを取得
      const sireCode = paramMap.get('sireCode') != null ? paramMap.get('sireCode') : queryParamMap.get('sireCode');
      // クエリパラメータから工場コードを取得
      const kojCode = StringUtils.defaultIfEmpty(queryParamMap.get('kojCode'), '');
      // クエリパラメータからpreEvent(遷移前の処理)を取得
      const preEvent = NumberUtils.toNumberDefaultIfEmpty(queryParamMap.get('preEvent'), null);

      let loadingToken = null;
      let isError = false;

      // ローディング開始
      this.loadingService.start().pipe(
        // ローディングトークン待避
        tap((token) => loadingToken = token),
        // メッセージをクリア
        tap(() => this.clearMessage()),
        // 画面非表示
        tap(() => this.invisibled = true),
        // フォームを生成
        tap(() => this.mainForm = this.createFormGroup()),
        // 非活性項目を設定
        tap(() => this.setDisabled()),
        // 画面表示に必要な翻訳テキストを取得
        flatMap(() => this.getInitialTranslate()),
        // preEvent(遷移前の処理)によってメッセージを設定
        tap(() => this.setPreEventMessage(preEvent)),
        // sireCodeが設定されている場合は、処理を続行
        filter(() => sireCode !== null),
        // 仕入先情報を取得
        flatMap(() => this.getMaintSire(sireCode, kojCode)),
        // エラーが発生した場合は、エラーモーダルを表示
        catchError((error) => {
          isError = true;
          return this.messageConfirmModalService.openErrorModal(error);
        }),
        finalize(() => {
          // 画面表示（エラーが発生した場合は、非表示のままとする）
          this.invisibled = isError;
          // ローディング停止
          this.loadingService.stop(loadingToken);
        })
      ).subscribe();
    });
  }

  ngOnDestroy() {
    // ローディングサブスクリプション停止
    this.loadingSubscription.unsubscribe();
  }

  /**
   * フォームを取得する。
   * @return mainForm.controls
   */
  get f(): any { return this.mainForm.controls; }

  /**
   * true:管轄ブランドコード重複エラーあり
   * @return boolean
   */
  get hasDupulicate(): boolean {

    const formControls = this.f;
    // 変換前ブランドリスト
    const bfoBrandList = [
      formControls.brand1.value,
      formControls.brand2.value,
      formControls.brand3.value,
      formControls.brand4.value,
      formControls.brand5.value,
      formControls.brand6.value,
      formControls.brand7.value,
      formControls.brand8.value,
      formControls.brand9.value,
      formControls.brand10.value,
      formControls.brand11.value,
      formControls.brand12.value,
      formControls.brand13.value,
      formControls.brand14.value,
      formControls.brand15.value,
      formControls.brand16.value,
      formControls.brand17.value,
      formControls.brand18.value,
      formControls.brand19.value,
      formControls.brand20.value,
      formControls.brand21.value,
      formControls.brand22.value,
      formControls.brand23.value,
      formControls.brand24.value,
      formControls.brand25.value,
      formControls.brand26.value,
      formControls.brand27.value,
      formControls.brand28.value,
      formControls.brand29.value,
      formControls.brand30.value
    ];

    // 変換前ブランドリストから空白要素を削除したリスト作成
    this.brandList = bfoBrandList.filter(value => { return value != '' });
    // Set型に変換し、重複削除
    const aftBrandList = new Set(this.brandList);
    // Set型の長さと元の配列の長さが異なる場合、trueを返す(重複エラーあり)
    return aftBrandList.size != this.brandList.length;
  }

  /**
   * エラー表示の有無を返却する。
   * @param value FormControl
   */
  isErrorDisplay(value: FormControl): boolean {
    return value.invalid && (value.dirty || value.touched);
  }

  /**
   * バリデーションの結果を取得する。バリデーションエラーがある場合、フッターにバリデーションエラーメッセージを表示する。
   * @returns バリデーションの結果
   * - true : 正常
   * - false : エラーあり
   */
  private isValid(): boolean {
    if (this.mainForm.invalid) {
      this.message.footer.error = { code: 'ERRORS.VALID_ERROR', param: null };
      FormUtils.markAsTouchedAllFields(this.mainForm);
      return false;
    }

    if (this.hasDupulicate) {
      this.message.footer.error = { code: 'ERRORS.VALID_ERROR', param: null };
      return false;
    }

    return true;
  }

  /**
   * メッセージをクリアする。
   */
  private clearMessage(): void {
    this.message = {
      footer: {
        success: { code: '', param: null },
        error: { code: '', param: null }
      }
    };
  }

  /**
   * 画面表示に必要な翻訳テキストを取得する。
   */
  private getInitialTranslate(): Observable<any> {
    return this.translateService.get('TITLE.SIRE').pipe(
      tap((title) => {
        // タイトルを取得
        this.title = title;
      }
      ));
  }

  /**
   * preEvent(遷移前の処理)によってメッセージを設定する。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   */
  private setPreEventMessage(preEvent: number): void {
    switch (preEvent) {
      case PreEventParam.CREATE:
        this.message.footer.success = { code: 'SUCSESS.ENTRY', param: { value: this.title } };
        break;
      case PreEventParam.UPDATE:
        this.message.footer.success = { code: 'SUCSESS.UPDATE', param: { value: this.title } };
        break;
      default:
        this.message.footer.success = { code: '', param: null };
        break;
    }
  }

  /**
   * フォームを作成する。
   */
  private createFormGroup(): FormGroup {
    const f = this.formBuilder.group({
      // 仕入先／工場／ＳＰＯＴ
      reckbn: [''],
      // 仕入先コード
      sireCode: ['', { validators: [Validators.pattern(ValidatorsPattern.NUMERIC_0_9)], updateOn: 'blur' }],
      // 仕入先名・正式名
      sireName: [''],
      // 工場コード
      kojCode: ['', { validators: [Validators.pattern(ValidatorsPattern.NUMERIC_0_9)], updateOn: 'blur' }],
      // 工場名・正式名
      kojName: ['', { updateOn: 'blur' }],
      // 工場名・省略名
      skojName: ['', { updateOn: 'blur' }],
      // 国内／国外
      inOut: [''],
      // 仕入先区分
      sirkbn: [''],
      // 管轄仕入先
      knktSire: [{value: '', disabled: true}],
      // 郵便番号
      yubin: ['', { validators: [Validators.pattern(/^[0-9]{3}-[0-9]{4}$/)], updateOn: 'blur' }],
      // 住所１
      add1: ['', { validators: [Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)], updateOn: 'blur' }],
      // 住所２
      add2: ['', { validators: [Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)], updateOn: 'blur' }],
      // 住所３
      add3: ['', { validators: [Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)], updateOn: 'blur' }],
      // 電話番号
      tel1: ['', { validators: [Validators.pattern(/^[0-9-()]*$/)], updateOn: 'blur' }],
      // 有害物質対応区分
      yugaikbn: [''],
      // 有害物質対応日付
      yugaiymd: [null],
      // 管轄ブランド1
      brand1: ['', { updateOn: 'blur' }],
      // 管轄ブランド2
      brand2: ['', { updateOn: 'blur' }],
      // 管轄ブランド3
      brand3: ['', { updateOn: 'blur' }],
      // 管轄ブランド4
      brand4: ['', { updateOn: 'blur' }],
      // 管轄ブランド5
      brand5: ['', { updateOn: 'blur' }],
      // 管轄ブランド6
      brand6: ['', { updateOn: 'blur' }],
      // 管轄ブランド7
      brand7: ['', { updateOn: 'blur' }],
      // 管轄ブランド8
      brand8: ['', { updateOn: 'blur' }],
      // 管轄ブランド9
      brand9: ['', { updateOn: 'blur' }],
      // 管轄ブランド10
      brand10: ['', { updateOn: 'blur' }],
      // 管轄ブランド11
      brand11: ['', { updateOn: 'blur' }],
      // 管轄ブランド12
      brand12: ['', { updateOn: 'blur' }],
      // 管轄ブランド13
      brand13: ['', { updateOn: 'blur' }],
      // 管轄ブランド14
      brand14: ['', { updateOn: 'blur' }],
      // 管轄ブランド15
      brand15: ['', { updateOn: 'blur' }],
      // 管轄ブランド16
      brand16: ['', { updateOn: 'blur' }],
      // 管轄ブランド17
      brand17: ['', { updateOn: 'blur' }],
      // 管轄ブランド18
      brand18: ['', { updateOn: 'blur' }],
      // 管轄ブランド19
      brand19: ['', { updateOn: 'blur' }],
      // 管轄ブランド20
      brand20: ['', { updateOn: 'blur' }],
      // 管轄ブランド21
      brand21: ['', { updateOn: 'blur' }],
      // 管轄ブランド22
      brand22: ['', { updateOn: 'blur' }],
      // 管轄ブランド23
      brand23: ['', { updateOn: 'blur' }],
      // 管轄ブランド24
      brand24: ['', { updateOn: 'blur' }],
      // 管轄ブランド25
      brand25: ['', { updateOn: 'blur' }],
      // 管轄ブランド26
      brand26: ['', { updateOn: 'blur' }],
      // 管轄ブランド27
      brand27: ['', { updateOn: 'blur' }],
      // 管轄ブランド28
      brand28: ['', { updateOn: 'blur' }],
      // 管轄ブランド29
      brand29: ['', { updateOn: 'blur' }],
      // 管轄ブランド30
      brand30: ['', { updateOn: 'blur' }],
      // 生地メーカー
      hkiji: [false],
      // 製品／縫製メーカー
      hseihin: [false],
      // 値札発注先
      hnefuda: [false],
      // 附属品メーカー
      hfuzoku: [false],
      // 発注書・送付方法
      hsofkbn: [''],
      // 発注書・メールアドレス
      hemail1: ['', { validators: [Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)], updateOn: 'blur' }],
      // 納品依頼・送付方法
      nsofkbn: [''],
      // 納品依頼・メールアドレス
      nemail1: ['', { validators: [Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)], updateOn: 'blur' }],
      // 受領書・送付方法
      ysofkbn: [''],
      // 受領書・メールアドレス
      yemail1: ['', { validators: [Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)], updateOn: 'blur' }],
    },
      {
        validator: Validators.compose([
        ])
      }
    );
    return f;
  }

  /**
   * フォームに値を設定する。
   * @param MaintSire 値
   */
  private setFormValue(MaintSire: MaintSire) {
    this.mainForm.patchValue({
      reckbn: MaintSire.reckbn,
      sireCode: MaintSire.sireCode,
      sireName: MaintSire.sireName,
      kojCode: MaintSire.kojCode,
      kojName: MaintSire.kojName,
      skojName: MaintSire.skojName,
      inOut: MaintSire.inOut == '' ? '0' : MaintSire.inOut,
      sirkbn: MaintSire.sirkbn,
      knktSire: MaintSire.knktSire,
      yubin: MaintSire.yubin,
      add1: MaintSire.add1,
      add2: MaintSire.add2,
      add3: MaintSire.add3,
      tel1: MaintSire.tel1,
      yugaikbn: MaintSire.yugaikbn,
      yugaiymd: DateUtils.parse(this.format(MaintSire.yugaiymd)),
      brand1: MaintSire.brand1,
      brand2: MaintSire.brand2,
      brand3: MaintSire.brand3,
      brand4: MaintSire.brand4,
      brand5: MaintSire.brand5,
      brand6: MaintSire.brand6,
      brand7: MaintSire.brand7,
      brand8: MaintSire.brand8,
      brand9: MaintSire.brand9,
      brand10: MaintSire.brand10,
      brand11: MaintSire.brand11,
      brand12: MaintSire.brand12,
      brand13: MaintSire.brand13,
      brand14: MaintSire.brand14,
      brand15: MaintSire.brand15,
      brand16: MaintSire.brand16,
      brand17: MaintSire.brand17,
      brand18: MaintSire.brand18,
      brand19: MaintSire.brand19,
      brand20: MaintSire.brand20,
      brand21: MaintSire.brand21,
      brand22: MaintSire.brand22,
      brand23: MaintSire.brand23,
      brand24: MaintSire.brand24,
      brand25: MaintSire.brand25,
      brand26: MaintSire.brand26,
      brand27: MaintSire.brand27,
      brand28: MaintSire.brand28,
      brand29: MaintSire.brand29,
      brand30: MaintSire.brand30,
      hkiji: MaintSire.hkiji == '1' ? true : false,
      hseihin: MaintSire.hseihin == '1' ? true : false,
      hnefuda: MaintSire.hnefuda == '1' ? true : false,
      hfuzoku: MaintSire.hfuzoku == '1' ? true : false,
      hsofkbn: MaintSire.hsofkbn,
      hemail1: MaintSire.hemail1,
      nsofkbn: MaintSire.nsofkbn,
      nemail1: MaintSire.nemail1,
      ysofkbn: MaintSire.ysofkbn,
      yemail1: MaintSire.yemail1
    });
    // disable、バリデーション設定
    this.setDisable();
    // 初期バリデーション設定
    this.setInitValidation();
  }

  /**
   * フォームの値をもとに型変換した値を取得する。
   * @returns 型変換後の値
   */
  private convertFrom(): MaintSire {
    const formControls = this.f;

    return {
      reckbn: formControls.reckbn.value,
      sireCode: formControls.sireCode.value,
      sireName: formControls.sireName.value,
      kojCode: formControls.kojCode.value,
      kojName: formControls.kojName.value,
      skojName: formControls.skojName.value,
      inOut: formControls.inOut.value == '0' ? '' : formControls.inOut.value,
      sirkbn: formControls.sirkbn.value,
      yubin: formControls.yubin.value,
      add1: formControls.add1.value,
      add2: formControls.add2.value,
      add3: formControls.add3.value,
      tel1: formControls.tel1.value,
      yugaikbn: formControls.yugaikbn.value,
      yugaiymd: DateUtils.format(formControls.yugaiymd.value).replace('/','').replace('/',''),
      brand1: this.brandList[0] === undefined ? '' : this.brandList[0],
      brand2: this.brandList[1] === undefined ? '' : this.brandList[1],
      brand3: this.brandList[2] === undefined ? '' : this.brandList[2],
      brand4: this.brandList[3] === undefined ? '' : this.brandList[3],
      brand5: this.brandList[4] === undefined ? '' : this.brandList[4],
      brand6: this.brandList[5] === undefined ? '' : this.brandList[5],
      brand7: this.brandList[6] === undefined ? '' : this.brandList[6],
      brand8: this.brandList[7] === undefined ? '' : this.brandList[7],
      brand9: this.brandList[8] === undefined ? '' : this.brandList[8],
      brand10: this.brandList[9] === undefined ? '' : this.brandList[9],
      brand11: this.brandList[10] === undefined ? '' : this.brandList[10],
      brand12: this.brandList[11] === undefined ? '' : this.brandList[11],
      brand13: this.brandList[12] === undefined ? '' : this.brandList[12],
      brand14: this.brandList[13] === undefined ? '' : this.brandList[13],
      brand15: this.brandList[14] === undefined ? '' : this.brandList[14],
      brand16: this.brandList[15] === undefined ? '' : this.brandList[15],
      brand17: this.brandList[16] === undefined ? '' : this.brandList[16],
      brand18: this.brandList[17] === undefined ? '' : this.brandList[17],
      brand19: this.brandList[18] === undefined ? '' : this.brandList[18],
      brand20: this.brandList[19] === undefined ? '' : this.brandList[19],
      brand21: this.brandList[20] === undefined ? '' : this.brandList[20],
      brand22: this.brandList[21] === undefined ? '' : this.brandList[21],
      brand23: this.brandList[22] === undefined ? '' : this.brandList[22],
      brand24: this.brandList[23] === undefined ? '' : this.brandList[23],
      brand25: this.brandList[24] === undefined ? '' : this.brandList[24],
      brand26: this.brandList[25] === undefined ? '' : this.brandList[25],
      brand27: this.brandList[26] === undefined ? '' : this.brandList[26],
      brand28: this.brandList[27] === undefined ? '' : this.brandList[27],
      brand29: this.brandList[28] === undefined ? '' : this.brandList[28],
      brand30: this.brandList[29] === undefined ? '' : this.brandList[29],
      hkiji: formControls.hkiji.value == false ? '0' : '1',
      hseihin: formControls.hseihin.value == false ? '0' : '1',
      hnefuda: formControls.hnefuda.value == false ? '0' : '1',
      hfuzoku: formControls.hfuzoku.value == false ? '0' : '1',
      hsofkbn: formControls.hsofkbn.value == '' ? '0' : formControls.hsofkbn.value,
      // メールアドレス: 空白を削除
      hemail1: StringUtils.deleteWhitespace(formControls.hemail1.value),
      nsofkbn: formControls.nsofkbn.value == '' ? '0' : formControls.nsofkbn.value,
      // メールアドレス: 空白を削除
      nemail1: StringUtils.deleteWhitespace(formControls.nemail1.value),
      ysofkbn: formControls.ysofkbn.value == '' ? '0' : formControls.ysofkbn.value,
      // メールアドレス: 空白を削除
      yemail1: StringUtils.deleteWhitespace(formControls.yemail1.value)
    } as MaintSire;
  }

  /**
   * 登録ボタン押下時の処理
   */
  onEntry(): void {
    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid()),
      // 仕入先を作成
      flatMap(() => this.maintSireService.create(this.convertFrom())),
      // 遷移先のURLを設定
      tap((response) => this.router.navigate(
        ['maint/sires', response.sireCode, Path.EDIT],
        { queryParams: { kojCode: response.kojCode, preEvent: PreEventParam.CREATE, t: new Date().valueOf() } })),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
      ).subscribe();
  }

  /**
   * 更新ボタン押下時の処理
   */
  onUpdate(): void {
    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid()),
      // 仕入先を更新
      flatMap(() => this.maintSireService.update(this.convertFrom())),
      // 遷移先のURLを設定
      tap((response) => this.router.navigate(
        ['maint/sires', response.sireCode, Path.EDIT],
        { queryParams: { kojCode: response.kojCode, preEvent: PreEventParam.UPDATE, t: new Date().valueOf() } })),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 削除ボタン押下時の処理
   */
  onDelete(): void {
    let loadingToken = null;

    // 確認モーダル表示
    this.messageConfirmModalService.translateAndOpenConfirmModal('INFO.DELETE_COMFIRM_MESSAGE', { value: this.title }).pipe(
      // 確認モーダルの結果が「OK」の場合、処理を続行
      filter((result) => result),
      // ローディング開始＆トークン待避
      flatMap(() => loadingToken = this.loadingService.start()),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // 仕入先を削除
      flatMap(() => this.maintSireService.delete(this.f.sireCode.value, this.f.kojCode.value, this.f.reckbn.value)),
      // 遷移先のURLを設定
      tap(() => this.router.navigate(['maint/sires'])),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 仕入先情報を取得する。
   * @param sireCode 仕入先コード
   * @param kojCode 工場コード
   */
  private getMaintSire(sireCode: String, kojCode: String): Observable<MaintSire> {
    // PRD_0168 add JFE start
    this.inOutList[0].checked = false;
    this.inOutList[1].checked = false;
    // PRD_0168 add JFE end
    return this.maintSireService.get(sireCode, kojCode).pipe(
      tap((response) => this.setDataToForm(response, this.reckbnList, this.inOutList)),
      tap((response) => this.setFormValue(response))
    );
  }

  /**
   * 選択データの区分をFormに設定する.
   * @param maintSire 検索フォーム値
   * @param reckbnList 区分リスト
   */
  private setDataToForm(maintSire: MaintSire, reckbnList: Kbn[], inOutList: Kbn[]): void {
    // 区分の初期値を設定
    reckbnList.filter(reckbn => reckbn.value == maintSire.reckbn).forEach(reckbn => reckbn.checked = true);
    // 国内・国外の初期値を設定
    // 国内''の場合、'0'に変換
    let inOutConvert = maintSire.inOut == '' ? '0' : maintSire.inOut;
    inOutList.filter(inOut => inOut.value == inOutConvert).forEach(inOut => inOut.checked = true);
  }

  /**
   * 'メールアドレス' フォーカスアウト時の処理。
   * @param value 入力値
   */
  onBlurMailAddress(value: string, type: string): void {
    this.mainForm.patchValue({ [type]: StringUtils.deleteWhitespace(value) });
  }

  /**
   * 日付フォーカスアウト時処理
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // patchValueしないとpipeの変換値がformにセットされない
    const ngbDate = DateUtils.onBlurDate(maskedValue);
    if (ngbDate) { this.mainForm.patchValue({ [type]: ngbDate }); }
  }

  /**
   * DBから取得した有害物質対応日付を「/」入りに変換する。
   * @param bfoDate 変換前有害物質対応日付
   * @param aftDate 変換後有害物質対応日付
   */
  private format(bfoDate: string): string {
    let aftDate = '';
    if ((StringUtils.isEmpty(bfoDate)) || (bfoDate == '00000000')) {
      return aftDate;
    }
    const year = bfoDate.slice(0, 4);
    const month = bfoDate.slice(4, 6);
    const day = bfoDate.slice(6,8);
    aftDate += year + '/';
    aftDate += month + '/';
    aftDate += day;
    return aftDate;
  }

  /**
   * ブランドコードリストを取得するAPIコール
   * @retruns ブランドコードリスト
   */
  private getBrand = (): Observable<GenericList<BrandCode>> =>
    this.junpcCodmstService.getBrandCodes(new BrandCodesSearchConditions()).pipe(
      tap(data => this.brandmstList = data.items)
    )

  /**
   * 編集不可項目を制御する。
   */
  private setDisabled(): void {
    if (this.path === Path.EDIT) {
      // 編集の場合、ヘッダ項目を非活性にする
      this.f.reckbn.disable(); // 区分
      this.f.sireCode.disable(); // 仕入先コード
      this.f.kojCode.disable(); // 工場コード
    }
  }

  /**
   * 条件によって、disableを設定する。
   */
  private setDisable(): void {
    const formControls = this.f;
    // 新規かつ、区分が仕入先
    if (this.path === Path.NEW && formControls.reckbn.value === '1') {
      formControls.kojCode.disable();
    }
  }

  /**
   * 区分ラジオボタン選択切り替え時の処理.
   * @param value 入力値
   */
  onChangeReckbnRadio(value: string): void {
    this.mainForm.patchValue({ reckbn: value });
    const formControls = this.f;
    this.setValidation(<FormGroup> formControls.kojName, [Validators.required]);
    // disable設定
    // 区分が仕入先
    if (formControls.reckbn.value === '1') {
      this.mainForm.patchValue({ kojCode: '' });
      formControls.kojCode.disable();
    }
    // 区分が工場orSPOT
    if (formControls.reckbn.value === '2' || formControls.reckbn.value === '3') {
      formControls.kojCode.enable();
    }
    // 区分がSPOT
    if (formControls.reckbn.value === '3') {
      this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.pattern(/^(ＳＰＯＴ|SPOT).*$/)]);
      // 国内
      if (formControls.inOut.value === '0') {
        this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.maxLength(20), Validators.pattern(/^(?=ＳＰＯＴ)[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
      }
      // 国外
      if (formControls.inOut.value === '1') {
        this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.pattern(/^(?=ＳＰＯＴ|SPOT)[^”",，\t|\n|\r|\r\n]*$/)]);
      }
    }
  }

  /**
   * 国内・国外ラジオボタン選択切り替え時の処理.
   * @param value 入力値
   */
  onChangeInOutRadio(value: string): void {
    this.mainForm.patchValue({ inOut: value });
    const formControls = this.f;
    this.setValidation(<FormGroup> formControls.kojName, [Validators.required]);
    this.setValidation(<FormGroup> formControls.skojName, [Validators.required]);
    this.setValidation(<FormGroup> formControls.yubin, [Validators.maxLength(8), Validators.pattern(/^[0-9]{3}-[0-9]{4}$/)]);
    this.setValidation(<FormGroup> formControls.add1, [Validators.maxLength(20), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
    // バリデーション設定
    // 国内
    if (formControls.inOut.value === '0') {
      this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.maxLength(20), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
      this.setValidation(<FormGroup> formControls.skojName, [Validators.required, Validators.maxLength(10), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
      // PRD_0169 JFE mod start
      //this.setValidation(<FormGroup> formControls.yubin, [Validators.maxLength(8), Validators.pattern(/^[0-9]{3}-[0-9]{4}$/)]);
      //this.setValidation(<FormGroup> formControls.add1, [Validators.maxLength(20), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
      this.setValidation(<FormGroup> formControls.yubin, [Validators.required, Validators.maxLength(8), Validators.pattern(/^[0-9]{3}-[0-9]{4}$/)]);
      this.setValidation(<FormGroup> formControls.add1, [Validators.required, Validators.maxLength(20), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
      // PRD_0169 JFE mod end
      // 区分がSPOT
      if (formControls.reckbn.value === '3') {
        this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.maxLength(20), Validators.pattern(/^(?=ＳＰＯＴ)[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
      }
    }
    // 国外
    if (formControls.inOut.value === '1') {
      this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.pattern(/^[^”",，\t|\n|\r|\r\n]*$/)]);
      this.setValidation(<FormGroup> formControls.skojName, [Validators.required, Validators.pattern(/^[^”",，\t|\n|\r|\r\n]*$/)]);
      // 区分がSPOT
      if (formControls.reckbn.value === '3') {
        this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.pattern(/^(?=ＳＰＯＴ|SPOT)[^”",，\t|\n|\r|\r\n]*$/)]);
      }
    }
  }

  /**
   * 発注書発送区分選択切り替え時の処理.
   * @param value 入力値
   */
  onChangeHsofkbn(value: string): void {
    this.mainForm.patchValue({ hsofkbn: value });
    const formControls = this.f;
    // PDFメール
    if (formControls.hsofkbn.value === '3') {
      this.setValidation(<FormGroup> formControls.hemail1, [Validators.required, Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    } else {
      this.setValidation(<FormGroup> formControls.hemail1, [Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    }
  }

  /**
   * 納品依頼発送区分選択切り替え時の処理.
   * @param value 入力値
   */
  onChangeNsofkbn(value: string): void {
    this.mainForm.patchValue({ nsofkbn: value });
    const formControls = this.f;
    // PDFメール
    if (formControls.nsofkbn.value === '3') {
      this.setValidation(<FormGroup> formControls.nemail1, [Validators.required, Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    } else {
      this.setValidation(<FormGroup> formControls.nemail1, [Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    }
  }

  /**
   * 受領書発送区分選択切り替え時の処理.
   * @param value 入力値
   */
  onChangeYsofkbn(value: string): void {
    this.mainForm.patchValue({ ysofkbn: value });
    const formControls = this.f;
    // PDFメール
    if (formControls.ysofkbn.value === '3') {
      this.setValidation(<FormGroup> formControls.yemail1, [Validators.required, Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    } else {
      this.setValidation(<FormGroup> formControls.yemail1, [Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    }
  }

  /**
   * 仕入先フォーカスアウト処理.
   * @param value 入力値
   */
  onBlurSireCode(value: string): void {
    this.mainForm.patchValue({ sireCode: value });
    const formControls = this.f;
    this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required]);
    if (formControls.sireCode.value) {
      // 仕入先コード一桁目
      const sireCode1 = formControls.sireCode.value.slice(0, 1);
      switch (sireCode1) {
        case '0':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^00$|^10$|^30$/)]);
          break;
        case '1':
        case '2':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^10$/)]);
          break;
        case '3':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^30$/)]);
          break;
        case '4':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^40$/)]);
          break;
        case '5':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^50$/)]);
          break;
        case '6':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^60$/)]);
          break;
        case '7':
        case '8':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^70$/)]);
          break;
        case '9':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^90$/)]);
          break;
        default:
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required]);
          break;
      }
    }
  }

  /**
   * バリデーションを設定する。
   * @param control FormGroup
   * @param validators ValidatorFn[]
   */
  private setValidation(control: FormGroup, validators?: ValidatorFn[]): void {
    control.clearValidators();
    control.updateValueAndValidity();
    if (validators) {
      control.setValidators(validators);
      control.updateValueAndValidity();
    }
  }

  /**
   * 初期バリデーションを設定する。
   */
  private setInitValidation(): void {
    const formControls = this.f;
    // 仕入先区分バリデーション設定
    // 仕入先コード一桁目
    const sireCode1 = formControls.sireCode.value.slice(0, 1);
      switch (sireCode1) {
        case '0':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^00$|^10$|^30$/)]);
          break;
        case '1':
        case '2':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^10$/)]);
          break;
        case '3':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^30$/)]);
          break;
        case '4':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^40$/)]);
          break;
        case '5':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^50$/)]);
          break;
        case '6':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^60$/)]);
          break;
        case '7':
        case '8':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^70$/)]);
          break;
        case '9':
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required, Validators.pattern(/^90$/)]);
          break;
        default:
          this.setValidation(<FormGroup> formControls.sirkbn, [Validators.required]);
          break;
      }

    // 正式名、略名、郵便番号、住所１バリデーション設定
    // 区分がSPOT
    if (formControls.reckbn.value === '3') {
      // 国内
      if (formControls.inOut.value === '0') {
        this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.maxLength(20), Validators.pattern(/^(?=ＳＰＯＴ)[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
        // PRD_0171 JFE add start
        this.setValidation(<FormGroup> formControls.skojName, [Validators.required, Validators.maxLength(10), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
        this.setValidation(<FormGroup> formControls.yubin, [Validators.required, Validators.maxLength(8), Validators.pattern(/^[0-9]{3}-[0-9]{4}$/)]);
        this.setValidation(<FormGroup> formControls.add1, [Validators.required, Validators.maxLength(20), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
        // PRD_0171 JFE add end
      }
      // 国外
      if (formControls.inOut.value === '1') {
        this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.pattern(/^(?=ＳＰＯＴ|SPOT)[^”",，\t|\n|\r|\r\n]*$/)]);
        // PRD_0171 JFE add start
        this.setValidation(<FormGroup> formControls.skojName, [Validators.required, Validators.pattern(/^[^”",，\t|\n|\r|\r\n]*$/)]);
        // PRD_0171 JFE add end
      }
    // 区分が仕入先か工場
    } else {
      // 国内
      if (formControls.inOut.value === '0') {
        this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.maxLength(20), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
        this.setValidation(<FormGroup> formControls.skojName, [Validators.required, Validators.maxLength(10), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
        this.setValidation(<FormGroup> formControls.yubin, [Validators.required, Validators.maxLength(8), Validators.pattern(/^[0-9]{3}-[0-9]{4}$/)]);
        this.setValidation(<FormGroup> formControls.add1, [Validators.required, Validators.maxLength(20), Validators.pattern(/^[^ -~｡-ﾟ”",，\t|\n|\r|\r\n]*$/)]);
      }
      // 国外
      if (formControls.inOut.value === '1') {
        this.setValidation(<FormGroup> formControls.kojName, [Validators.required, Validators.pattern(/^[^”",，\t|\n|\r|\r\n]*$/)]);
        this.setValidation(<FormGroup> formControls.skojName, [Validators.required, Validators.pattern(/^[^”",，\t|\n|\r|\r\n]*$/)]);
      }
    }

    // メールアドレスバリデーション設定
    // PDFメール
    if (formControls.hsofkbn.value === '3') {
      this.setValidation(<FormGroup> formControls.hemail1, [Validators.required, Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    }
    // PDFメール
    if (formControls.nsofkbn.value === '3') {
      this.setValidation(<FormGroup> formControls.nemail1, [Validators.required, Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    }
    // PDFメール
    if (formControls.ysofkbn.value === '3') {
      this.setValidation(<FormGroup> formControls.yemail1, [Validators.required, Validators.pattern(ValidatorsPattern.EMAIL_COMMA_DELIMITED)]);
    }
  }
}
