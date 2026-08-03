import request from '@/utils/request'

export function createStreamSession(data) {
  return request.post('/llm/stream', data)
}

export function getConfigs() {
  return request.get('/config')
}
