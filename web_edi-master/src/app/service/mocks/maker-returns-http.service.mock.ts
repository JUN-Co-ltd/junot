import { GenericList } from 'src/app/model/generic-list';
import { MakerReturnSearchResult } from 'src/app/model/maker-return-search-result';
import { LgSendType } from 'src/app/const/lg-send-type';

// tslint:disable:max-line-length
const makerReturnSearchResults = [
  // { voucherNumber: '000000', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00000', supplierName: '信興　太郎', returnLot: 1, amount: 1000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000001', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00001', supplierName: '信興　一郎', returnLot: 11, amount: 11000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000002', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00002', supplierName: '信興　二郎', returnLot: 12, amount: 12000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000003', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00003', supplierName: '信興　三郎', returnLot: 13, amount: 13000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000004', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00004', supplierName: '信興　四郎', returnLot: 14, amount: 14000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000005', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00005', supplierName: '信興　五郎', returnLot: 15, amount: 15000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000006', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00006', supplierName: '信興　六郎', returnLot: 16, amount: 16000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000007', lgSendType: LgSendType.NO_INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00007', supplierName: '信興　七郎', returnLot: 17, amount: 17000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000008', lgSendType: LgSendType.INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00008', supplierName: '信興　八郎', returnLot: 18, amount: 18000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000009', lgSendType: LgSendType.INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00009', supplierName: '信興　九郎', returnLot: 19, amount: 9000, voucherNumberInputAt: new Date() },
  // { voucherNumber: '000010', lgSendType: LgSendType.INSTRUCTION as LgSendType, voucherNumberAt: new Date(), supplierCode: '00010', supplierName: '信興　十郎', returnLot: 9999999999, amount: 9999999999, voucherNumberInputAt: new Date() },
];

export const makerReturnSearchResultMock = {
  items: makerReturnSearchResults,
  nextPageToken: 'aaaaa'
} as GenericList<MakerReturnSearchResult>;
