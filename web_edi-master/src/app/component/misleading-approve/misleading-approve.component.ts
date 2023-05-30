import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, AbstractControl, FormArray } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { TranslateService } from '@ngx-translate/core';

import { Observable, of, from, combineLatest, forkJoin, Subscription } from 'rxjs';
import { tap, flatMap, catchError, finalize, distinct, toArray, filter } from 'rxjs/operators';

import {
  Path, PreEventParam, CompositionsCommon, YugaiKbnDictionary, SeasonDictionary, QualityApprovalStatus, MSirmstYugaikbnType
} from 'src/app/const/const';

import { NumberUtils } from 'src/app/util/number-utils';
import { ListUtils } from 'src/app/util/list-utils';
import { BusinessCheckUtils } from 'src/app/util/business-check-utils';

import { Authority } from 'src/app/enum/authority.enum';

import { HeaderService } from 'src/app/service/header.service';
import { SessionService } from 'src/app/service/session.service';
import { MisleadingRepresentationService } from 'src/app/service/misleading-representation.service';
import { MisleadingApproveFormService } from 'src/app/service/form/misleading-approve-form.service';
import { FileService } from 'src/app/service/file.service';
import { JunpcCodmstService } from 'src/app/service/junpc-codmst.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';

import { Sku } from 'src/app/model/sku';
import { Compositions } from 'src/app/model/compositions';
import { ItemMisleadingRepresentation } from 'src/app/model/item-misleading-representation';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';

@Component({
  selector: 'app-misleading-approve',
  templateUrl: './misleading-approve.component.html',
  styleUrls: ['./misleading-approve.component.scss'],
  providers: [
    MisleadingApproveFormService
  ]
})
export class MisleadingApproveComponent implements OnInit, OnDestroy {

  // HTML参照用
  readonly YUGAI_KBN = YugaiKbnDictionary;
  readonly UNSUBMITTED: MSirmstYugaikbnType = MSirmstYugaikbnType.UNSUBMITTED;
  readonly SEASON = SeasonDictionary;

  /** フォーム */
  mainForm: FormGroup;

  /** Submit処理のメッセージタイトル */
  private messageTitle: string;

  /** 画面を非表示にする */
  invisibled = true;

  /** 画面を非活性にする */
  disabled = true;

  /** エラーモーダル表示後に画面を非表示にする */
  afterInvisibled = true;

  /** ローディング用のサブスクリプション */
  private loadingSubscription: Subscription;

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

  /** 優良誤認承認画面用のデータ */
  misleadingRepresentation: ItemMisleadingRepresentation;

  /** 優良誤認承認画面用データのSKUでカラーの重複を除去したリスト */
  distinctColorSkus: Sku[];

  /** QA権限ではないか */
  notQaAuthority = true;

  /** 優良誤認ステータスが非対象か */
  isQualityStatusNonTarget = {
    coo: true,
    composition: true,
    harmful: true,
    all: true
  };

  /** 選択中のSKU(カラー) */
  selectedSku: Sku;

  /** 選択中のSKU(カラー)の組成リスト */
  selectedCompositions: Compositions[];

  /** 遷移先URL */
  nextUrl = '';

  constructor(
    private headerService: HeaderService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private router: Router,
    private route: ActivatedRoute,
    private misleadingApproveFormService: MisleadingApproveFormService,
    private translateService: TranslateService,
    private sessionService: SessionService,
    private fileService: FileService,
    private junpcCodmstService: JunpcCodmstService,
    private misleadingRepresentationService: MisleadingRepresentationService
  ) { }

  /**
   * @return this.mainForm.controls
   */
  get fCtrl(): { [key: string]: AbstractControl } {
    return this.mainForm.controls;
  }

  /**
   * @return this.mainForm.getRawValue()
   */
  get fVal(): any {
    return this.mainForm.getRawValue();
  }

  /**
   * @returns this.mainForm.get('compositionInspections')
   */
  get compositionInspectionsArray(): FormArray {
    return this.mainForm.get('compositionInspections') as FormArray;
  }

  /**
   * @returns (<FormArray> this.mainForm.get('compositionInspections')).controls
   */
  get fCtrlCompositionInspections(): AbstractControl[] {
    return (<FormArray> this.mainForm.get('compositionInspections')).controls;
  }

  ngOnInit() {
    this.headerService.show();
    this.loadingService.clear();
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.disabled = isLoading);

    combineLatest([
      this.route.paramMap,
      this.route.queryParamMap
    ]).subscribe(([paramMap, queryParamMap]) => {
      this.nextUrl = this.getNextUrl(this.route);
      const partNoId = NumberUtils.toNumberDefaultIfEmpty(paramMap.get('id'), null);
      const preEvent = NumberUtils.toNumberDefaultIfEmpty(queryParamMap.get('preEvent'), null);
      const session = this.sessionService.getSaveSession();
      this.notQaAuthority = !session.authorities.some(auth => Authority.ROLE_QA === auth);
      this.misleadingApproveFormService.approvalUserAccountName = session.accountName;
      let loadingToken = null;
      let isError = false;

      this.loadingService.start().pipe(
        tap(token => loadingToken = token),
        tap(() => this.clearMessage()),
        tap(() => this.invisibled = true),
        flatMap(this.getInitialTranslate),
        tap(() => this.setPreEventMessage(preEvent)),
        filter(() => partNoId !== null),
        flatMap(() =>
          forkJoin(
            this.getStaffName(session.accountName),
            this.getMisleadingRepresentation(partNoId)
          )
        ),
        tap(([_, misleadingRepresentation]) => this.createForm(misleadingRepresentation)),
        catchError(error => {
          isError = true;
          return this.messageConfirmModalService.openErrorModal(error);
        }),
        finalize(() => {
          this.invisibled = isError;
          this.loadingService.stop(loadingToken);
        })
      ).subscribe();
    });
  }

  ngOnDestroy(): void {
    this.loadingSubscription.unsubscribe();
  }

  /**
   * 次画面に遷移するためのURL取得.
   *
   * @param route ActivatedRoute
   * @returns URL
   */
  private getNextUrl(route: ActivatedRoute): string {
    // 親のURLの先頭のパスを取得
    const parentUrl = route.parent.snapshot.url[0].path;

    if (route.snapshot.url.length === 2) {
      // 自分自身のURLのパスの個数が2つの場合、親のURLを返却する
      return parentUrl;
    }

    // 親のURL＋自分自身のURLの先頭のパスを返却する
    return parentUrl + '/' + route.snapshot.url[0].path;
  }

  /**
   * 画面表示に必要な翻訳テキストを取得する.
   * @returns Observable<void>
   */
  private getInitialTranslate = (): Observable<void> =>
    this.translateService.get('TITLE.MISLEADING_REPRESENTATION').pipe(tap(title => this.messageTitle = title))

  /**
   * preEvent(遷移前の処理)によってメッセージを設定する.
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   */
  private setPreEventMessage = (preEvent: number): void => {
    switch (preEvent) {
      case PreEventParam.UPDATE:
        this.message.footer.success = { code: 'SUCSESS.UPDATE', param: { value: this.messageTitle } };
        break;
      default:
        this.message.footer.success = { code: '', param: null };
        break;
    }
  }

  /**
   * 優良誤認承認画面用データ取得処理.
   * @param partNoId 品番ID
   * @returns Observable<ItemMisleadingRepresentation>
   */
  private getMisleadingRepresentation = (partNoId: number): Observable<ItemMisleadingRepresentation> =>
    this.misleadingRepresentationService.get(partNoId).pipe(
      tap(data => {
        this.misleadingRepresentation = data;
        this.setIsStatusNonTarget(data);
        if (ListUtils.isNotEmpty(data.skus)) { this.onSelectSkuTab(data.skus[0]); }
        from(data.skus).pipe(
          distinct(sku => sku.colorCode),
          toArray()
        ).subscribe(skus => this.distinctColorSkus = skus);
      }))

  /**
   * 社員名取得処理.
   * @param accountName アカウント名
   * @returns Observable<JunpcCodmst>
   */
  private getStaffName = (accountName: string): Observable<JunpcCodmst> =>
    this.junpcCodmstService.getStaff(accountName).pipe(
      tap(data => this.misleadingApproveFormService.approvalUserName = data.item2)
    )

  /**
   * 優良誤認ステータス設定処理.
   * @param data 優良誤認承認画面用データ
   */
  private setIsStatusNonTarget = (data: ItemMisleadingRepresentation): void => {
    this.isQualityStatusNonTarget.coo = QualityApprovalStatus.NON_TARGET === data.qualityCooStatus;
    this.isQualityStatusNonTarget.composition = QualityApprovalStatus.NON_TARGET === data.qualityCompositionStatus;
    this.isQualityStatusNonTarget.harmful = QualityApprovalStatus.NON_TARGET === data.qualityHarmfulStatus;
    this.isQualityStatusNonTarget.all = BusinessCheckUtils.isAllQualityStatusNonTarget(data);
  }

  /**
   * SKUタブ選択時の処理.
   * @param sku 選択したSKU
   */
  onSelectSkuTab(sku: Sku): void {
    // SKU設定
    this.selectedSku = sku;

    // 組成設定
    const compositions = this.misleadingRepresentation.compositions;
    if (ListUtils.isNotEmpty(compositions)) {
      this.selectedCompositions = compositions.filter(comp => comp.colorCode === sku.colorCode);
      // 指定したカラーがない場合は共通を設定
      if (ListUtils.isEmpty(this.selectedCompositions)) {
        this.setCommonToSelectComposition(compositions);
      }
    }
  }

  /**
   * 共通の組成を選択中に設定する.
   * @param compositions 組成リスト
   */
  private setCommonToSelectComposition = (compositions: Compositions[]): void => {
    if (ListUtils.isEmpty(compositions)) { return; }
    this.selectedCompositions = compositions.filter(this.isCommonComposition);
  }

  /**
   * 共通の組成か判定する.
   * @param composition 組成
   * @returns true:共通
   */
  private isCommonComposition = (compotiosion: Compositions): boolean => compotiosion.colorCode === CompositionsCommon.COLOR_CODE;

  /**
   * フォームを作成する.
   * @param data 取得した優良誤認情報
   * @returns Observable<void>
   */
  private createForm = (data: ItemMisleadingRepresentation): Observable<void> => {
    // フォームを生成
    this.mainForm = this.misleadingApproveFormService.generateDataSettedForm(data);
    return of(null);
  }

  /**
   * 全カラーチェックボックス押下時の処理.
   * @param check チェック状態
   */
  onCheckAllColor = (check: boolean): void =>
    this.fCtrlCompositionInspections.forEach(comp => {
      comp.patchValue({ check: check });
      this.onCheck(check, comp);
    })

  /**
   * チェックボックス押下時の処理.
   * @param check チェック状態
   * @param fCtrl フォームコントロール
   */
  onCheck = (check: boolean, fCtrl: AbstractControl): void => this.misleadingApproveFormService.onCheck(check, fCtrl);

  /**
   * Submitボタン押下時の処理.
   */
  onSubmit(): void {
    let loadingToken = null;
    this.loadingService.start().pipe(
      tap(token => loadingToken = token),
      tap(this.clearMessage),
      flatMap(() => this.misleadingRepresentationService.put(this.mainForm.getRawValue())),
      tap(this.routeAfterSubmit),
      catchError(error => this.messageConfirmModalService.openErrorModal(error)),
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * Submit後の遷移処理.
   * @param response レスポンス
   */
  private routeAfterSubmit = (response: ItemMisleadingRepresentation): void => {
    this.router.navigate(
      [this.nextUrl, response.id, Path.EDIT],  // id：品番ID
      { queryParams: { preEvent: PreEventParam.UPDATE, t: new Date().valueOf() } });
  }

  /**
   * ファイルリンク押下時の処理.
   * @param fileNoId ファイルID
   * @returns Subscription
   */
  onFileDownlad = (fileNoId: number): Subscription =>
    this.fileService.downloadFile(fileNoId).subscribe(() => { },
      (err: Error) => this.message.footer.error.code = err.message)

  /**
   * メッセージをクリアする.
   */
  private clearMessage = (): void => {
    this.message = {
      footer: {
        success: { code: '', param: null },
        error: { code: '', param: null }
      }
    };
  }

  /**
   * @returns true:更新ボタン非活性
   */
  isDisableUpdateBtn(): boolean | null {
    return this.disabled
      || this.notQaAuthority
      || this.isQualityStatusNonTarget.all
      || this.misleadingRepresentation.readOnly
      ? true : null;
  }
}
