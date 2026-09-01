export type PriorityLead = {
  number: string
  name: string
  priority: string
  action: string
  person?: string
  when: string
  sector: string
  temperature: string
}

export type ProspectingTask = {
  id: number
  title: string
  company: string
  due: string
  time: string
  priority: string
  done: boolean
}

export const priorityLeads: PriorityLead[] = [
  {
    number: '01',
    name: 'Celisano',
    priority: 'A+',
    action: 'Visita presencial: hablar con Ignacio y conseguir reunión de diagnóstico.',
    person: 'Ignacio',
    when: '21 AGO',
    sector: 'Alimentos / distribución',
    temperature: 'WARM',
  },
  {
    number: '02',
    name: 'Estudio Manzor',
    priority: 'A',
    action: 'Validar alcance, revisar preview y definir aprobación del presupuesto.',
    when: 'PRIORIDAD',
    sector: 'Estudio contable',
    temperature: 'HOT',
  },
  {
    number: '03',
    name: 'Hotel Los Cedros',
    priority: 'A+',
    action: 'Primer contacto cuidadoso con la dueña, desde web y canal directo.',
    when: 'PENDIENTE',
    sector: 'Hotelería / eventos',
    temperature: 'WARM',
  },
]

export const initialProspectingTasks: ProspectingTask[] = [
  { id: 1, title: 'Visita de diagnóstico', company: 'Celisano', due: '21 AGO', time: '12:00', priority: 'A+', done: false },
  { id: 2, title: 'Validar alcance y preview', company: 'Estudio Manzor', due: 'Hoy', time: '—', priority: 'A', done: false },
  { id: 3, title: 'Primer contacto web/canal directo', company: 'Hotel Los Cedros', due: 'Pendiente', time: '—', priority: 'A+', done: false },
  { id: 4, title: 'Presentar propuesta de canal directo', company: 'Hotel Manantiales', due: 'Pendiente', time: '—', priority: 'A', done: false },
]

export const pipelineStages = [
  ['Nuevas', '12 oportunidades', '—'],
  ['Calificadas', '1 oportunidad', '—'],
  ['Propuesta', '1 oportunidad', '$45.000 UYU'],
  ['Cerradas perdidas', '3 oportunidades', '—'],
] as const
