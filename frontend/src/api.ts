export type ApiCompany = {
  id: string
  ownerId?: string
  name: string
  website?: string
  address?: string
  phone?: string
  whatsapp?: string
  email?: string
  industry?: string
  city?: string
  country?: string
  source?: string
  status: string
}

export type ApiContact = {
  id: string
  companyId: string
  firstName: string
  lastName?: string
  role?: string
  email?: string
  phone?: string
  instagram?: string
  linkedin?: string
  notes?: string
}

export type ApiOpportunity = {
  id: string
  ownerId?: string
  companyId: string
  contactId?: string
  name: string
  stage: string
  estimatedValue?: number
  currency?: string
  probability?: number
  expectedCloseDate?: string
  nextAction?: string
  nextActionDate?: string
  lostReason?: string
}

export type ApiLead = {
  id: string
  companyId: string
  companyName: string
  contactId?: string
  source?: string
  temperature: string
  status: string
  priority?: string
  dataQuality?: string
  nextContactAt?: string
  score?: number
  summary?: string
  createdAt?: string
  updatedAt?: string
}

export type ApiTask = {
  id: string
  companyId: string
  title: string
  description?: string
  status: string
  priority: string
  dueAt?: string
  completedAt?: string
  assignedTo?: string
  source?: string
  done: boolean
  createdAt?: string
  updatedAt?: string
}

export type ApiDashboard = {
  opportunitiesRequiringAttention: number
  overdueFollowUps: number
  proposalsAwaitingResponse: number
  openPipelineValue: number
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
    ...init,
  })
  if (!response.ok) {
    let detail = `Error ${response.status}`
    try {
      const body = await response.json()
      if (body?.detail) detail = String(body.detail)
    } catch {
      // ignore non-JSON errors
    }
    throw new Error(detail)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

const json = (method: string, body: unknown): RequestInit => ({
  method,
  body: JSON.stringify(body),
})

export const api = {
  companies: {
    list: () => request<ApiCompany[]>('/api/companies'),
    create: (body: Record<string, unknown>) => request<ApiCompany>('/api/companies', json('POST', body)),
    remove: (id: string) => request<void>(`/api/companies/${id}`, { method: 'DELETE' }),
  },
  contacts: {
    list: (companyId?: string) => request<ApiContact[]>(companyId ? `/api/contacts?companyId=${encodeURIComponent(companyId)}` : '/api/contacts'),
    create: (body: Record<string, unknown>) => request<ApiContact>('/api/contacts', json('POST', body)),
    remove: (id: string) => request<void>(`/api/contacts/${id}`, { method: 'DELETE' }),
  },
  leads: {
    list: () => request<ApiLead[]>('/api/crm/leads'),
    create: (body: Record<string, unknown>) => request<ApiLead>('/api/crm/leads', json('POST', body)),
    remove: (id: string) => request<void>(`/api/crm/leads/${id}`, { method: 'DELETE' }),
  },
  opportunities: {
    list: () => request<ApiOpportunity[]>('/api/opportunities'),
    create: (body: Record<string, unknown>) => request<ApiOpportunity>('/api/opportunities', json('POST', body)),
    move: (id: string, stage: string) => request<ApiOpportunity>(`/api/opportunities/${id}/stage`, json('PATCH', { stage })),
    remove: (id: string) => request<void>(`/api/opportunities/${id}`, { method: 'DELETE' }),
  },
  tasks: {
    list: () => request<ApiTask[]>('/api/crm/tasks'),
    create: (body: Record<string, unknown>) => request<ApiTask>('/api/crm/tasks', json('POST', body)),
    toggle: (id: string, done: boolean) => request<ApiTask>(`/api/crm/tasks/${id}`, json('PATCH', { done })),
    remove: (id: string) => request<void>(`/api/crm/tasks/${id}`, { method: 'DELETE' }),
  },
  dashboard: {
    today: () => request<ApiDashboard>('/api/dashboard/today'),
  },
}
