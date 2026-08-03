/**
 * 文本导出工具：TXT / Markdown
 */
export function exportAsTxt(content, filename = 'novel') {
  downloadFile(content, `${filename}.txt`, 'text/plain;charset=utf-8')
}

export function exportAsMarkdown(title, content, filename = 'novel') {
  const md = `# ${title}\n\n${content}`
  downloadFile(md, `${filename}.md`, 'text/markdown;charset=utf-8')
}

function downloadFile(content, filename, mimeType) {
  const blob = new Blob([content], { type: mimeType })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

export async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    return true
  }
}

export function splitParagraphs(content) {
  return content.split(/\n{2,}/).filter(Boolean)
}
