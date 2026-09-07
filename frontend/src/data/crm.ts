export type Company = {
  id: string
  name: string
  industry: string | null
  city: string | null
  country: string
  phone?: string
  whatsapp?: string
  email?: string
}

export type Contact = {
  id: string
  companyId: string
  name: string
  role?: string
  email?: string
  phone?: string
}

export type Lead = {
  id: string
  companyId: string
  contactId?: string
  stage: string
  temperature: 'COLD' | 'WARM' | 'HOT'
  priority: string
  source: string
  summary: string
  quality: 'HIGH' | 'INCOMPLETE'
}

export type OpportunityStage = 'NEW' | 'QUALIFIED' | 'PROPOSAL' | 'NEGOTIATION' | 'WON' | 'LOST'

export type Opportunity = {
  id: string
  companyId: string
  contactId?: string
  name: string
  stage: OpportunityStage
  value?: number
  currency?: 'UYU' | 'USD'
  nextAction?: string
}

export type CrmTask = {
  id: string
  companyId: string
  title: string
  description?: string
  due: string
  priority: string
  done: boolean
}
