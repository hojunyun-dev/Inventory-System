import React, { useState, useEffect } from 'react';
import api from '../services/api';

interface Category {
  id: number;
  name: string;
  description: string;
  level: number;
  parent?: Category;
  children?: Category[];
}

interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  partType: string;
  partCondition: string;
  manufacturerName: string;
  oemPartNumber?: string;
  aftermarketPartNumber?: string;
  isOeQuality: boolean;
  isAftermarket: boolean;
  category: Category;
}

interface VehicleCompatibility {
  id: number;
  manufacturer: string;
  model: string;
  yearStart: number;
  yearEnd: number;
  engineType: string;
  transmission: string;
  trim: string;
  notes: string;
}

interface CatalogStats {
  totalProducts: number;
  activeProducts: number;
  oeQualityProducts: number;
  aftermarketProducts: number;
  totalCategories: number;
  totalVehicleCompatibilities: number;
  partTypeStats: { [key: string]: number };
}

const CatalogTest: React.FC = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [vehicleCompatibilities, setVehicleCompatibilities] = useState<VehicleCompatibility[]>([]);
  const [stats, setStats] = useState<CatalogStats | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 검색 필터 상태
  const [searchFilters, setSearchFilters] = useState({
    keyword: '',
    partType: '',
    manufacturer: '',
    isOeQuality: null as boolean | null,
    isAftermarket: null as boolean | null,
    minPrice: '',
    maxPrice: ''
  });

  // 데이터 로드 함수들
  const loadCategories = async () => {
    try {
      const response = await api.get('/catalog/categories');
      setCategories(response.data);
    } catch (err) {
      console.error('카테고리 로드 실패:', err);
    }
  };

  const loadProducts = async () => {
    try {
      const response = await api.get('/catalog/products/search/advanced', {
        params: {
          keyword: searchFilters.keyword || undefined,
          partType: searchFilters.partType || undefined,
          manufacturer: searchFilters.manufacturer || undefined,
          isOeQuality: searchFilters.isOeQuality,
          isAftermarket: searchFilters.isAftermarket,
          minPrice: searchFilters.minPrice || undefined,
          maxPrice: searchFilters.maxPrice || undefined
        }
      });
      setProducts(response.data);
    } catch (err) {
      console.error('제품 로드 실패:', err);
      setError('제품 데이터를 불러오는데 실패했습니다.');
    }
  };

  const loadVehicleCompatibilities = async () => {
    try {
      const response = await api.get('/catalog/vehicle-compatibility');
      setVehicleCompatibilities(response.data);
    } catch (err) {
      console.error('차량 호환성 로드 실패:', err);
    }
  };

  const loadStats = async () => {
    try {
      const response = await api.get('/catalog/stats');
      setStats(response.data);
    } catch (err) {
      console.error('통계 로드 실패:', err);
    }
  };

  useEffect(() => {
    const loadAllData = async () => {
      setLoading(true);
      try {
        await Promise.all([
          loadCategories(),
          loadProducts(),
          loadVehicleCompatibilities(),
          loadStats()
        ]);
      } catch (err) {
        setError('데이터 로드 중 오류가 발생했습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadAllData();
  }, []);

  useEffect(() => {
    loadProducts();
  }, [searchFilters]);

  const handleFilterChange = (field: string, value: any) => {
    setSearchFilters(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const clearFilters = () => {
    setSearchFilters({
      keyword: '',
      partType: '',
      manufacturer: '',
      isOeQuality: null,
      isAftermarket: null,
      minPrice: '',
      maxPrice: ''
    });
  };

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="text-center">
          <div className="spinner-border" role="status">
            <span className="sr-only">로딩 중...</span>
          </div>
          <p>카탈로그 데이터를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">카탈로그 시스템 테스트</h1>
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* 통계 정보 */}
      {stats && (
        <div className="bg-blue-50 p-4 rounded-lg mb-6">
          <h2 className="text-xl font-semibold mb-3">카탈로그 통계</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">{stats.totalProducts}</div>
              <div className="text-sm text-gray-600">총 제품</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">{stats.oeQualityProducts}</div>
              <div className="text-sm text-gray-600">OE 품질</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-orange-600">{stats.aftermarketProducts}</div>
              <div className="text-sm text-gray-600">애프터마켓</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-purple-600">{stats.totalCategories}</div>
              <div className="text-sm text-gray-600">카테고리</div>
            </div>
          </div>
        </div>
      )}

      {/* 검색 필터 */}
      <div className="bg-white p-4 rounded-lg shadow mb-6">
        <h2 className="text-xl font-semibold mb-3">고급 검색</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">키워드</label>
            <input
              type="text"
              value={searchFilters.keyword}
              onChange={(e) => handleFilterChange('keyword', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="제품명, 설명, 부품번호 검색"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">부품 타입</label>
            <select
              value={searchFilters.partType}
              onChange={(e) => handleFilterChange('partType', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">전체</option>
              <option value="ENGINE">엔진</option>
              <option value="BRAKE">브레이크</option>
              <option value="SUSPENSION">서스펜션</option>
              <option value="ELECTRICAL">전기</option>
              <option value="FILTER">필터</option>
              <option value="LUBRICANT">윤활유</option>
              <option value="IGNITION">점화</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">제조사</label>
            <input
              type="text"
              value={searchFilters.manufacturer}
              onChange={(e) => handleFilterChange('manufacturer', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="제조사명"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">최소 가격</label>
            <input
              type="number"
              value={searchFilters.minPrice}
              onChange={(e) => handleFilterChange('minPrice', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">최대 가격</label>
            <input
              type="number"
              value={searchFilters.maxPrice}
              onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="999999"
            />
          </div>
          <div className="flex items-end">
            <button
              onClick={clearFilters}
              className="w-full bg-gray-500 text-white px-4 py-2 rounded-md hover:bg-gray-600"
            >
              필터 초기화
            </button>
          </div>
        </div>
        <div className="mt-4 flex gap-2">
          <button
            onClick={() => handleFilterChange('isOeQuality', true)}
            className={`px-4 py-2 rounded-md ${
              searchFilters.isOeQuality === true 
                ? 'bg-blue-500 text-white' 
                : 'bg-gray-200 text-gray-700'
            }`}
          >
            OE 품질만
          </button>
          <button
            onClick={() => handleFilterChange('isAftermarket', true)}
            className={`px-4 py-2 rounded-md ${
              searchFilters.isAftermarket === true 
                ? 'bg-orange-500 text-white' 
                : 'bg-gray-200 text-gray-700'
            }`}
          >
            애프터마켓만
          </button>
        </div>
      </div>

      {/* 제품 목록 */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-4 border-b">
          <h2 className="text-xl font-semibold">제품 목록 ({products.length}개)</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">제품 정보</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">부품 정보</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가격/재고</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">품질</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {products.map((product) => (
                <tr key={product.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">{product.name}</div>
                      <div className="text-sm text-gray-500">{product.sku}</div>
                      <div className="text-xs text-gray-400">{product.description}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm text-gray-900">{product.partType}</div>
                      <div className="text-sm text-gray-500">{product.manufacturerName}</div>
                      {product.oemPartNumber && (
                        <div className="text-xs text-blue-600">OEM: {product.oemPartNumber}</div>
                      )}
                      {product.aftermarketPartNumber && (
                        <div className="text-xs text-orange-600">AM: {product.aftermarketPartNumber}</div>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div>
                      <div className="text-sm font-medium text-gray-900">₩{product.price.toLocaleString()}</div>
                      <div className="text-sm text-gray-500">재고: {product.quantity}개</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex flex-col gap-1">
                      {product.isOeQuality && (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                          OE 품질
                        </span>
                      )}
                      {product.isAftermarket && (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
                          애프터마켓
                        </span>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 차량 호환성 */}
      <div className="mt-6 bg-white rounded-lg shadow">
        <div className="p-4 border-b">
          <h2 className="text-xl font-semibold">차량 호환성 ({vehicleCompatibilities.length}개)</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">제조사/모델</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">연식</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">엔진/변속기</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">트림</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {vehicleCompatibilities.map((compatibility) => (
                <tr key={compatibility.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">{compatibility.manufacturer}</div>
                    <div className="text-sm text-gray-500">{compatibility.model}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {compatibility.yearStart} - {compatibility.yearEnd || '현재'}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{compatibility.engineType}</div>
                    <div className="text-sm text-gray-500">{compatibility.transmission}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{compatibility.trim}</div>
                    {compatibility.notes && (
                      <div className="text-xs text-gray-500">{compatibility.notes}</div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* 카테고리 계층 구조 */}
      <div className="mt-6 bg-white rounded-lg shadow">
        <div className="p-4 border-b">
          <h2 className="text-xl font-semibold">카테고리 계층 구조</h2>
        </div>
        <div className="p-4">
          <div className="space-y-2">
            {categories
              .filter(cat => cat.level === 0)
              .map(category => (
                <CategoryTree key={category.id} category={category} allCategories={categories} />
              ))}
          </div>
        </div>
      </div>
    </div>
  );
};

// 카테고리 트리 컴포넌트
const CategoryTree: React.FC<{ category: Category; allCategories: Category[] }> = ({ category, allCategories }) => {
  const children = allCategories.filter(cat => cat.parent?.id === category.id);
  
  return (
    <div className="ml-4">
      <div className="flex items-center">
        <span className="text-sm font-medium text-gray-900">
          {category.name} (Level {category.level})
        </span>
        {category.description && (
          <span className="ml-2 text-xs text-gray-500">- {category.description}</span>
        )}
      </div>
      {children.map(child => (
        <CategoryTree key={child.id} category={child} allCategories={allCategories} />
      ))}
    </div>
  );
};

export default CatalogTest;
