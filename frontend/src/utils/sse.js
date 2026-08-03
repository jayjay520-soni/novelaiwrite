/**
 * SSE 流式接收封装，实现打字机效果
 */
export class SseClient {
  constructor(url, options = {}) {
    this.url = url
    this.options = options
    this.eventSource = null
    this.onMessage = options.onMessage || (() => {})
    this.onDone = options.onDone || (() => {})
    this.onError = options.onError || (() => {})
  }

  connect() {
    this.eventSource = new EventSource(this.url)

    this.eventSource.onmessage = (event) => {
      if (event.data === '[DONE]') {
        this.onDone()
        this.close()
        return
      }
      this.onMessage(event.data)
    }

    this.eventSource.addEventListener('done', () => {
      this.onDone()
      this.close()
    })

    this.eventSource.addEventListener('error', (event) => {
      if (event.data) {
        this.onError(event.data)
      }
    })

    this.eventSource.onerror = () => {
      if (this.eventSource.readyState === EventSource.CLOSED) {
        this.onDone()
      } else {
        this.onError('SSE 连接异常')
      }
      this.close()
    }
  }

  close() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
  }
}

export function buildStreamUrl(clientId, params, token) {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value != null && value !== '') {
      searchParams.append(key, value)
    }
  })
  searchParams.append('token', token)
  return `/api/llm/stream/${clientId}?${searchParams.toString()}`
}
