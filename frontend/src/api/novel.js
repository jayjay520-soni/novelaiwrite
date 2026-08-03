import request from '@/utils/request'

export function createNovel(data) {
  return request.post('/novel', data)
}

export function updateNovel(id, data) {
  return request.put(`/novel/${id}`, data)
}

export function deleteNovel(id) {
  return request.delete(`/novel/${id}`)
}

export function getNovelDetail(id, config = {}) {
  return request.get(`/novel/${id}`, config)
}

export function getNovelList() {
  return request.get('/novel/list')
}

export function generateAsync(id, data) {
  return request.post(`/novel/${id}/generate-async`, data)
}

export function exportNovel(id, format = 'txt') {
  return request.post(`/novel/${id}/export?format=${format}`)
}
