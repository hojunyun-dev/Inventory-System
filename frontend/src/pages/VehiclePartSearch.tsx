import React, { useState } from 'react';
import {
  Container, Paper, Typography, Box, TextField, Button, Grid,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Chip, Alert, CircularProgress, FormControl, InputLabel, Select, MenuItem,
} from '@mui/material';
import { Search as SearchIcon, DirectionsCar as CarIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Product, PartType } from '../types';

// 제조사별 차량 모델 데이터
const vehicleModels: { [key: string]: string[] } = {
  '현대': ['소나타', '아반떼', '그랜저', '투싼', '싼타페', '팰리세이드', '코나', '아이오닉', '넥쏘', '스타리아', '포터', '마이티'],
  '기아': ['K5', 'K7', 'K8', 'K9', '스포티지', '쏘렌토', '모하비', '카니발', '니로', 'EV6', '스팅어', '레이', '모닝', '봉고'],
  '쉐보레': ['크루즈', '말리부', '임팔라', '카마로', '콜벳', '트래버스', '이쿼녹스', '트레일블레이저', '스파크', '아베오', '크루즈'],
  'GM': ['캐딜락', 'GMC', '허머', '사부르', '폰티악', '올즈모빌', '뷰익', '쉐보레'],
  '르노삼성': ['SM3', 'SM5', 'SM6', 'SM7', 'QM3', 'QM5', 'QM6', 'XM3'],
  '쌍용': ['코란도', '렉스턴', '체어맨', '무쏘', '로디우스', '액티언', '체어맨W', '체어맨H'],
  'BMW': ['3시리즈', '5시리즈', '7시리즈', 'X1', 'X3', 'X5', 'X7', 'Z4', 'i3', 'i8', 'iX'],
  '벤츠': ['C클래스', 'E클래스', 'S클래스', 'A클래스', 'CLA', 'CLS', 'GLA', 'GLC', 'GLE', 'GLS', 'AMG'],
  '아우디': ['A3', 'A4', 'A6', 'A8', 'Q3', 'Q5', 'Q7', 'Q8', 'TT', 'R8', 'e-tron'],
  '폭스바겐': ['골프', '제타', '파사트', '티구안', '투렉', '비틀', 'CC', '아테온', '샤란', 'ID.3', 'ID.4'],
  '도요타': ['캠리', '아발론', 'RAV4', '하이랜더', '프리우스', '코롤라', '시에나', '타코마', '터널', '렉서스'],
  '혼다': ['아코드', '시빅', 'CR-V', '파일럿', '리지', '인사이트', '핏', '엘리멘트', '오디세이', 'NSX'],
  '닛산': ['알티마', '센트라', '무라노', '패스파인더', '로그', '리프', 'GT-R', '370Z', '큐브', '인피니티'],
  '마쓰다': ['마쓰다3', '마쓰다6', 'CX-3', 'CX-5', 'CX-9', 'MX-5', 'RX-7', 'RX-8', '프리메시'],
  '스바루': ['임프레자', '레거시', '아웃백', '포레스터', '트리베카', 'WRX', 'STI', 'BRZ'],
  '미쓰비시': ['랜서', '갈란트', '아웃랜더', '이클립스', '엔데버', '몬테로', '미라지'],
  '스즈키': ['스위프트', '그랜드비타라', 'SX4', 'Kizashi', 'Jimny', 'Alto', 'WagonR'],
  '다이하쓰': ['테리오스', '자즈', '미라', '쿠오레', '캐스터', '무브', '로키'],
  '이스즈': ['뮤', '엘프', '포워드', '기가', 'D-MAX', 'MU-X'],
  '포드': ['포커스', '피에스타', '몬데오', '쿠가', '익스플로러', '익스페디션', 'F-150', '머스탱'],
  '크라이슬러': ['300C', '그랜드체로키', '체로키', '컴퍼스', '파시피카', '타운앤컨트리'],
  '지프': ['체로키', '그랜드체로키', '랭글러', '컴퍼스', '리니티', '글래디에이터', '그랜드왜건니어'],
  '캐딜락': ['ATS', 'CTS', 'XTS', 'CT6', 'XT4', 'XT5', 'XT6', '에스컬레이드', 'SRX'],
  '링컨': ['MKZ', 'MKS', 'MKT', 'MKX', 'MKC', '컨티넨탈', '네비게이터'],
  '부가티': ['베이론', '치론', '디보', '센토디에치'],
  '람보르기니': ['우라칸', '아벤타도르', '우루스', '허라칸'],
  '페라리': ['488', 'F8', 'SF90', '라페라리', '포르토피노', '로마', 'GTC4루소'],
  '마세라티': ['지블리', '쿠페', '그란투리스모', '레반테', '퀴포르테', '그란카브리오'],
  '알파로메오': ['줄리에타', '미토', '4C', '스텔비오', '줄리아', '토날레'],
  '피아트': ['500', '500L', '500X', '판다', '티포', '브라보', '크로마'],
  '랜드로버': ['디스커버리', '디스커버리스포츠', '레인지로버', '레인지로버이보크', '레인지로버벨라', '디펜더'],
  '재규어': ['XE', 'XF', 'XJ', 'F-PACE', 'E-PACE', 'I-PACE', 'F-TYPE'],
  '볼보': ['S60', 'S90', 'V40', 'V60', 'V90', 'XC40', 'XC60', 'XC90'],
  '사브': ['9-3', '9-5', '9-7X'],
  '시트로엥': ['C3', 'C4', 'C5', 'C6', 'DS3', 'DS4', 'DS5', '베를링고'],
  '푸조': ['208', '308', '408', '508', '2008', '3008', '5008', '파트너'],
  '르노': ['클리오', '메간', '라구나', '카포', '트윙고', '캉고'],
  '스코다': ['파비아', '옥타비아', '수퍼브', '카미크', '코디악', '카로크'],
  'DS': ['DS3', 'DS4', 'DS5', 'DS7', 'DS9'],
  '오펠': ['코르사', '아스트라', '인시그니아', '크로스랜드', '그랜드랜드'],
  '메르세데스-벤츠': ['A클래스', 'B클래스', 'C클래스', 'E클래스', 'S클래스', 'CLA', 'CLS', 'GLA', 'GLC', 'GLE', 'GLS'],
  '스마트': ['포투', '포포', '로드스터', '포사'],
  '미니': ['쿠퍼', '쿠퍼S', '쿠퍼SE', '컨트리맨', '클럽맨', '로드스터'],
  '롤스로이스': ['팬텀', '고스트', '윈드', '던', '컬리넨'],
  '벤틀리': ['컨티넨탈', '플라잉스퍼', '뮬산', '벤테이가'],
  '아스턴마틴': ['V8밴티지', 'V12밴티지', 'DB11', 'DBS', '랩터', '발키리'],
  '맥라렌': ['540C', '570S', '600LT', '720S', '아르투라', 'P1', '센나'],
  '로터스': ['엘리스', '엑시지', '에보라', '에미라']
};

const VehiclePartSearch: React.FC = () => {
  const navigate = useNavigate();
  const [manufacturer, setManufacturer] = useState('');
  const [model, setModel] = useState('');
  const [year, setYear] = useState('');
  const [selectedPartType, setSelectedPartType] = useState<PartType | ''>('');
  const [partName, setPartName] = useState('');
  const [results, setResults] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searched, setSearched] = useState(false);

  // 제조사 변경 시 모델 초기화
  const handleManufacturerChange = (selectedManufacturer: string) => {
    setManufacturer(selectedManufacturer);
    setModel(''); // 모델 초기화
  };

  const handleSearch = async () => {
    // 차종 정보가 모두 있으면 차종별 검색
    if (manufacturer && model && year) {
      await handleVehicleSearch();
    }
    // 차종 정보가 없고 부품명만 있으면 부품명 검색
    else if (partName.trim()) {
      await handlePartSearch();
    }
    // 둘 다 없으면 에러
    else {
      setError('차종 정보(제조사, 모델, 연식) 또는 부품명을 입력해주세요.');
    }
  };

  const handleVehicleSearch = async () => {
    try {
      setLoading(true);
      setError('');
      setSearched(true);

      // 1. 먼저 해당 차량 정보 검색
      const vehicleResponse = await axios.get(
        `http://localhost:8080/api/vehicle-compatibilities/search`,
        {
          params: { manufacturer, model, year: parseInt(year) },
          withCredentials: true
        }
      );

      if (vehicleResponse.data.length === 0) {
        setResults([]);
        return;
      }

      // 2. 각 차량 정보에 맞는 제품들 조회
      const vehicleIds = vehicleResponse.data.map((v: any) => v.id);
      const allProducts: Product[] = [];

      for (const vehicleId of vehicleIds) {
        try {
          const productsResponse = await axios.get(
            `http://localhost:8080/api/vehicle-compatibilities/${vehicleId}/products`,
            { withCredentials: true }
          );

          // API에서 받은 부품 정보를 직접 사용
          for (const product of productsResponse.data) {
            if (!allProducts.some(p => p.sku === product.sku)) {
              // 백엔드 응답을 Product 타입에 맞게 변환
              const convertedProduct: Product = {
                id: parseInt(product.sku.replace(/\D/g, '')) || Math.random() * 10000, // SKU에서 숫자 추출
                sku: product.sku,
                name: product.name,
                description: product.description,
                price: product.price,
                cost: product.price * 0.6, // 추정 비용
                barcode: `BC${product.sku}`,
                partType: product.partType,
                oemPartNumber: product.sku,
                manufacturerCode: 'HYUNDAI',
                warrantyMonths: product.warrantyMonths || 12,
                weight: 1.0,
                dimensions: '10x10x10cm',
                isSerialized: false,
                isActive: true,
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
              };
              allProducts.push(convertedProduct);
            }
          }
        } catch (err) {
          console.error('Failed to fetch products:', err);
        }
      }

      // 3. 부품명이나 카테고리로 추가 필터링
      let filteredProducts = allProducts;

      if (partName.trim()) {
        filteredProducts = filteredProducts.filter(product =>
          product.name.toLowerCase().includes(partName.toLowerCase())
        );
      }

      if (selectedPartType) {
        filteredProducts = filteredProducts.filter(product =>
          product.partType === selectedPartType
        );
      }

      setResults(filteredProducts);
    } catch (err) {
      setError('검색 중 오류가 발생했습니다.');
      console.error('Search error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handlePartSearch = async () => {
    try {
      setLoading(true);
      setError('');
      setSearched(true);

      // 부품명으로 직접 검색
      const response = await axios.get(
        `http://localhost:8080/api/products/search`,
        {
          params: { 
            name: partName,
            partType: selectedPartType || undefined
          },
          withCredentials: true,
          paramsSerializer: {
            indexes: null
          }
        }
      );

      setResults(response.data);
    } catch (err) {
      setError('검색 중 오류가 발생했습니다.');
      console.error('Search error:', err);
    } finally {
      setLoading(false);
    }
  };

  const getPartTypeLabel = (partType?: PartType): string => {
    const labels: Record<PartType, string> = {
      [PartType.ENGINE]: '엔진',
      [PartType.TRANSMISSION]: '변속기',
      [PartType.BRAKE]: '브레이크',
      [PartType.SUSPENSION]: '서스펜션',
      [PartType.ELECTRICAL]: '전기',
      [PartType.EXHAUST]: '배기',
      [PartType.COOLING]: '냉각',
      [PartType.FUEL]: '연료',
      [PartType.INTERIOR]: '실내',
      [PartType.EXTERIOR]: '외장',
      [PartType.TIRE_WHEEL]: '타이어',
      [PartType.LIGHTING]: '조명',
      [PartType.FILTER]: '필터',
      [PartType.FLUID]: '오일/액',
      [PartType.BODY]: '차체',
      [PartType.CLIMATE_CONTROL]: '공조',
      [PartType.STEERING]: '조향',
      [PartType.SAFETY]: '안전',
      [PartType.ACCESSORY]: '악세서리',
      [PartType.OTHER]: '기타',
    };
    return partType ? labels[partType] : '-';
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <CarIcon fontSize="large" /> 차종별 부품 검색
      </Typography>

      {/* 검색 폼 */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={2}>
            <FormControl fullWidth>
              <InputLabel>제조사</InputLabel>
              <Select
                value={manufacturer}
                label="제조사"
                onChange={(e) => handleManufacturerChange(e.target.value)}
              >
                <MenuItem value="">전체</MenuItem>
                <MenuItem value="현대">현대</MenuItem>
                <MenuItem value="기아">기아</MenuItem>
                <MenuItem value="쉐보레">쉐보레</MenuItem>
                <MenuItem value="GM">GM</MenuItem>
                <MenuItem value="르노삼성">르노삼성</MenuItem>
                <MenuItem value="쌍용">쌍용</MenuItem>
                <MenuItem value="BMW">BMW</MenuItem>
                <MenuItem value="벤츠">벤츠</MenuItem>
                <MenuItem value="아우디">아우디</MenuItem>
                <MenuItem value="폭스바겐">폭스바겐</MenuItem>
                <MenuItem value="도요타">도요타</MenuItem>
                <MenuItem value="혼다">혼다</MenuItem>
                <MenuItem value="닛산">닛산</MenuItem>
                <MenuItem value="마쓰다">마쓰다</MenuItem>
                <MenuItem value="스바루">스바루</MenuItem>
                <MenuItem value="미쓰비시">미쓰비시</MenuItem>
                <MenuItem value="스즈키">스즈키</MenuItem>
                <MenuItem value="다이하쓰">다이하쓰</MenuItem>
                <MenuItem value="이스즈">이스즈</MenuItem>
                <MenuItem value="포드">포드</MenuItem>
                <MenuItem value="크라이슬러">크라이슬러</MenuItem>
                <MenuItem value="지프">지프</MenuItem>
                <MenuItem value="캐딜락">캐딜락</MenuItem>
                <MenuItem value="링컨">링컨</MenuItem>
                <MenuItem value="부가티">부가티</MenuItem>
                <MenuItem value="람보르기니">람보르기니</MenuItem>
                <MenuItem value="페라리">페라리</MenuItem>
                <MenuItem value="마세라티">마세라티</MenuItem>
                <MenuItem value="알파로메오">알파로메오</MenuItem>
                <MenuItem value="피아트">피아트</MenuItem>
                <MenuItem value="랜드로버">랜드로버</MenuItem>
                <MenuItem value="재규어">재규어</MenuItem>
                <MenuItem value="볼보">볼보</MenuItem>
                <MenuItem value="사브">사브</MenuItem>
                <MenuItem value="시트로엥">시트로엥</MenuItem>
                <MenuItem value="푸조">푸조</MenuItem>
                <MenuItem value="르노">르노</MenuItem>
                <MenuItem value="스코다">스코다</MenuItem>
                <MenuItem value="DS">DS</MenuItem>
                <MenuItem value="오펠">오펠</MenuItem>
                <MenuItem value="메르세데스-벤츠">메르세데스-벤츠</MenuItem>
                <MenuItem value="스마트">스마트</MenuItem>
                <MenuItem value="미니">미니</MenuItem>
                <MenuItem value="롤스로이스">롤스로이스</MenuItem>
                <MenuItem value="벤틀리">벤틀리</MenuItem>
                <MenuItem value="아스턴마틴">아스턴마틴</MenuItem>
                <MenuItem value="맥라렌">맥라렌</MenuItem>
                <MenuItem value="로터스">로터스</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={2}>
            <FormControl fullWidth>
              <InputLabel>모델</InputLabel>
              <Select
                value={model}
                label="모델"
                onChange={(e) => setModel(e.target.value)}
                disabled={!manufacturer}
              >
                <MenuItem value="">전체</MenuItem>
                {manufacturer && vehicleModels[manufacturer]?.map((modelName) => (
                  <MenuItem key={modelName} value={modelName}>
                    {modelName}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={2}>
            <FormControl fullWidth>
              <InputLabel>연식</InputLabel>
              <Select
                value={year}
                label="연식"
                onChange={(e) => setYear(e.target.value)}
              >
                <MenuItem value="">전체</MenuItem>
                {Array.from({ length: new Date().getFullYear() - 1990 + 1 }, (_, i) => {
                  const yearValue = new Date().getFullYear() - i;
                  return (
                    <MenuItem key={yearValue} value={yearValue.toString()}>
                      {yearValue}년
                    </MenuItem>
                  );
                })}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={3}>
            <TextField
              fullWidth
              label="부품명 (선택사항)"
              value={partName}
              onChange={(e) => setPartName(e.target.value)}
              placeholder="예: 브레이크 패드, 엔진 오일"
            />
          </Grid>
          <Grid item xs={12} md={2}>
            <FormControl fullWidth>
              <InputLabel>부품 카테고리</InputLabel>
              <Select
                value={selectedPartType}
                onChange={(e) => setSelectedPartType(e.target.value as PartType | '')}
                label="부품 카테고리"
              >
                <MenuItem value="">전체</MenuItem>
                {Object.values(PartType).map((partType) => (
                  <MenuItem key={partType} value={partType}>
                    {getPartTypeLabel(partType)}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={1}>
            <Button
              fullWidth
              variant="contained"
              size="large"
              startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <SearchIcon />}
              onClick={handleSearch}
              disabled={loading}
            >
              {loading ? '검색 중...' : '검색'}
            </Button>
          </Grid>
        </Grid>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
      </Paper>

      {/* 검색 결과 */}
      {searched && (
        <Paper>
          <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
            <Typography variant="h6">
              검색 결과: {results.length}개 부품
            </Typography>
            {manufacturer && model && year && (
              <Typography variant="body2" color="textSecondary">
                {manufacturer} {model} ({year}년식) 호환 부품
                {partName && ` - "${partName}" 필터링`}
                {selectedPartType && ` (${getPartTypeLabel(selectedPartType)})`}
              </Typography>
            )}
            {!manufacturer && !model && !year && partName && (
              <Typography variant="body2" color="textSecondary">
                "{partName}" 검색 결과
                {selectedPartType && ` (${getPartTypeLabel(selectedPartType)})`}
              </Typography>
            )}
          </Box>

          {results.length === 0 ? (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <Typography color="textSecondary">
                해당 차량에 맞는 부품이 없습니다.
              </Typography>
            </Box>
          ) : (
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>부품명</TableCell>
                    <TableCell>SKU</TableCell>
                    <TableCell>OEM 번호</TableCell>
                    <TableCell>부품 유형</TableCell>
                    <TableCell>가격</TableCell>
                    <TableCell>작업</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {results.map((product) => (
                    <TableRow key={product.id} hover>
                      <TableCell>
                        <Typography variant="body2" fontWeight="bold">
                          {product.name}
                        </Typography>
                        {product.isSerialized && (
                          <Chip label="시리얼관리" size="small" color="secondary" sx={{ mt: 0.5 }} />
                        )}
                      </TableCell>
                      <TableCell>{product.sku}</TableCell>
                      <TableCell>{product.oemPartNumber || '-'}</TableCell>
                      <TableCell>
                        {product.partType ? (
                          <Chip label={getPartTypeLabel(product.partType)} size="small" />
                        ) : '-'}
                      </TableCell>
                      <TableCell>{product.price?.toLocaleString()}원</TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() => navigate(`/products/${product.id}`)}
                        >
                          상세보기
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </Paper>
      )}
    </Container>
  );
};

export default VehiclePartSearch;

