import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { ColorSize } from 'src/app/model/color-size';

export const NO_01_COLORS = [
  { code1: '00', item1: 'カラー０' },
  { code1: '02', item1: 'カラー２' },
  { code1: '03', item1: 'カラー３' },
  { code1: '04', item1: 'カラー４' },
  { code1: '05', item1: 'カラー５' },
] as JunpcCodmst[];

export const SKUS = [
  { colorCode: '00' },
  { colorCode: '01' },
  { colorCode: '01' },
  { colorCode: '01' },
  { colorCode: '03' },
  { colorCode: '04' },
  { colorCode: '04' },
  { colorCode: '06' }
] as ColorSize[];
