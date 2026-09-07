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

export const initialCompanies: Company[] = [
  { id: 'celisano', name: 'Celisano', industry: 'Alimentos / distribución', city: 'Salto', country: 'Uruguay', phone: '+598 92 244 124', email: 'info@celisano.com.uy' },
  { id: 'los-cedros', name: 'Hotel Los Cedros', industry: 'Hotelería / eventos', city: 'Salto', country: 'Uruguay', phone: '+598 4733 3984', whatsapp: '+598 98 031 290', email: 'loscedros@loscedros.com.uy' },
  { id: 'manantiales', name: 'Hotel Manantiales Termal', industry: 'Hotelería', city: 'Termas del Daymán', country: 'Uruguay', phone: '+598 4736 9832', whatsapp: '+598 98 675 457', email: 'hotelmanantiales@vera.com.uy' },
  { id: 'archi', name: 'Bungalows Archi', industry: 'Alojamiento turístico', city: 'Termas del Daymán', country: 'Uruguay', phone: '+598 4736 9512' },
  { id: 'manzor', name: 'Estudio Manzor', industry: 'Estudio contable', city: null, country: 'Uruguay' },
  { id: 'don-andres', name: 'Don Andrés', industry: 'Gastronomía', city: 'Montevideo', country: 'Uruguay', phone: '+598 2336 6418', email: 'clientes@donandres.uy' },
  { id: 'cocina-pedro', name: 'La Cocina de Pedro', industry: 'Gastronomía', city: 'Montevideo', country: 'Uruguay', phone: '+598 93 507 618 / +598 2413 7453', whatsapp: '+598 99 813 923', email: 'reservas@lacocinadepedro.com.uy' },
  { id: 'tandory', name: 'Tandory Restaurante', industry: 'Gastronomía', city: 'Montevideo', country: 'Uruguay', phone: '+598 2709 6616', whatsapp: '+598 94 596 735', email: 'tandoryrestaurant@gmail.com' },
  { id: 'primuseum', name: 'Primuseum', industry: 'Turismo / gastronomía / experiencias', city: 'Montevideo', country: 'Uruguay', phone: '+598 92 125 698', email: 'info@primuseum.com' },
  { id: 'el-fogon', name: 'El Fogón', industry: 'Gastronomía', city: 'Montevideo', country: 'Uruguay', phone: '+598 2900 0900', email: 'elfogon@elfogon.com.uy' },
  { id: 'francis', name: 'Francis Restaurante', industry: 'Gastronomía', city: 'Montevideo', country: 'Uruguay', phone: '+598 2711 8603', whatsapp: '+598 97 043 191', email: 'info@francis.com.uy' },
  { id: 'holiday-inn', name: 'Holiday Inn Montevideo / El Quijote', industry: 'Hotelería / gastronomía', city: 'Montevideo', country: 'Uruguay', phone: '+598 2902 0001', email: 'reservas@himontevideo.com.uy' },
  { id: 'uruguay-pleno', name: 'Uruguay Pleno', industry: null, city: null, country: 'Uruguay' },
  { id: 'panda-cou', name: 'Panda Cou', industry: null, city: null, country: 'Colombia' },
  { id: 'squania', name: 'Squania Suite', industry: null, city: null, country: 'Uruguay' },
  { id: 'acruxs', name: 'Acruxs', industry: null, city: null, country: 'Uruguay' },
  { id: 'paramuno', name: 'Paramuno', industry: null, city: null, country: 'Uruguay' },
]

export const initialContacts: Contact[] = [
  { id: 'ignacio', companyId: 'celisano', name: 'Ignacio', email: 'info@celisano.com.uy', phone: '+598 92 244 124' },
  { id: 'gabriel-coquel', companyId: 'tandory', name: 'Gabriel Coquel', role: 'Dirección / gerencia', email: 'tandoryrestaurant@gmail.com' },
  { id: 'alberto-latarowski', companyId: 'francis', name: 'Alberto Latarowski', email: 'info@francis.com.uy' },
]

export const initialLeads: Lead[] = [
  { id: 'lead-celisano', companyId: 'celisano', contactId: 'ignacio', stage: 'NEW', temperature: 'WARM', priority: 'A+', source: 'PROSPECCION_DIRECTA', quality: 'HIGH', summary: 'Visita presencial; no cotizar antes del diagnóstico.' },
  { id: 'lead-cedros', companyId: 'los-cedros', stage: 'NEW', temperature: 'WARM', priority: 'A+', source: 'REFERIDO_PERSONAL', quality: 'HIGH', summary: 'Entrar por web y canal directo; primer contacto cuidadoso con dirección.' },
  { id: 'lead-manantiales', companyId: 'manantiales', stage: 'NEW', temperature: 'WARM', priority: 'A', source: 'PROSPECCION_DAYMAN', quality: 'HIGH', summary: 'Propuesta de canal directo; PMS solo si aparece interés operativo.' },
  { id: 'lead-archi', companyId: 'archi', stage: 'NEW', temperature: 'WARM', priority: 'A', source: 'PROSPECCION_DAYMAN', quality: 'HIGH', summary: 'Contactar al dueño por el número de recepción.' },
  { id: 'lead-manzor', companyId: 'manzor', stage: 'PROPOSAL', temperature: 'HOT', priority: 'A', source: 'REFERRAL', quality: 'HIGH', summary: 'Validar alcance, preview y aprobación del presupuesto.' },
  { id: 'lead-don-andres', companyId: 'don-andres', stage: 'NEW', temperature: 'COLD', priority: 'A+', source: 'PROSPECCION_VERIFICADA', quality: 'HIGH', summary: 'Llamar y pedir dueño, gerente o responsable comercial/digital.' },
  { id: 'lead-pedro', companyId: 'cocina-pedro', stage: 'NEW', temperature: 'COLD', priority: 'A', source: 'PROSPECCION_VERIFICADA', quality: 'HIGH', summary: 'WhatsApp o llamada buscando responsable web/digital.' },
  { id: 'lead-tandory', companyId: 'tandory', contactId: 'gabriel-coquel', stage: 'NEW', temperature: 'COLD', priority: 'B', source: 'PROSPECCION_VERIFICADA', quality: 'HIGH', summary: 'Pedir por Gabriel o dirección. No vender rediseño web.' },
  { id: 'lead-primuseum', companyId: 'primuseum', stage: 'NEW', temperature: 'COLD', priority: 'B', source: 'PROSPECCION_VERIFICADA', quality: 'HIGH', summary: 'Buscar propietarios, dirección o responsable comercial.' },
  { id: 'lead-fogon', companyId: 'el-fogon', stage: 'NEW', temperature: 'COLD', priority: 'B', source: 'PROSPECCION_VERIFICADA', quality: 'HIGH', summary: 'Llamar o enviar correo buscando gerencia u operaciones.' },
  { id: 'lead-francis', companyId: 'francis', contactId: 'alberto-latarowski', stage: 'NEW', temperature: 'COLD', priority: 'C', source: 'PROSPECCION_VERIFICADA', quality: 'HIGH', summary: 'Pedir por Alberto, dirección u operaciones/sistemas. No vender web.' },
  { id: 'lead-holiday', companyId: 'holiday-inn', stage: 'NEW', temperature: 'COLD', priority: 'C', source: 'PROSPECCION_VERIFICADA', quality: 'HIGH', summary: 'Buscar dirección, operaciones, F&B o sistemas. No vender web.' },
  { id: 'lead-uruguay-pleno', companyId: 'uruguay-pleno', stage: 'FOLLOW_UP', temperature: 'WARM', priority: 'B', source: 'OUTBOUND', quality: 'INCOMPLETE', summary: 'Recuperar contacto y oportunidad exacta antes de automatizar acciones.' },
  { id: 'lead-panda', companyId: 'panda-cou', stage: 'QUALIFIED', temperature: 'WARM', priority: 'B', source: 'OUTBOUND_LATAM', quality: 'INCOMPLETE', summary: 'Retomar conversación con jefe/decisor.' },
]

export const initialOpportunities: Opportunity[] = [
  { id: 'opp-celisano', companyId: 'celisano', contactId: 'ignacio', name: 'Software a medida y automatización operativa', stage: 'NEW', nextAction: 'Visita de diagnóstico con Ignacio' },
  { id: 'opp-cedros', companyId: 'los-cedros', name: 'Modernización web y reservas directas', stage: 'NEW', nextAction: 'Primer contacto con dirección' },
  { id: 'opp-manantiales', companyId: 'manantiales', name: 'Web, reservas directas e integración hotelera', stage: 'NEW' },
  { id: 'opp-archi', companyId: 'archi', name: 'Web propia y reducción de dependencia OTA', stage: 'NEW' },
  { id: 'opp-manzor', companyId: 'manzor', name: 'Renovación web y captación comercial', stage: 'PROPOSAL', value: 45000, currency: 'UYU', nextAction: 'Validar alcance y preview' },
  { id: 'opp-don-andres', companyId: 'don-andres', name: 'Web, menú, reservas y SEO local', stage: 'NEW' },
  { id: 'opp-pedro', companyId: 'cocina-pedro', name: 'Web, SEO técnico, reservas y canal directo', stage: 'NEW' },
  { id: 'opp-tandory', companyId: 'tandory', contactId: 'gabriel-coquel', name: 'Integraciones, CRM y automatización interna', stage: 'NEW' },
  { id: 'opp-primuseum', companyId: 'primuseum', name: 'CRM turístico, multidioma e integraciones', stage: 'NEW' },
  { id: 'opp-fogon', companyId: 'el-fogon', name: 'Reservas, eventos y automatización', stage: 'NEW' },
  { id: 'opp-francis', companyId: 'francis', contactId: 'alberto-latarowski', name: 'Integraciones, reporting y CRM', stage: 'NEW' },
  { id: 'opp-holiday', companyId: 'holiday-inn', name: 'Integración hotel-restaurante y eventos', stage: 'NEW' },
  { id: 'opp-panda', companyId: 'panda-cou', name: 'Desarrollo web / software', stage: 'QUALIFIED', nextAction: 'Retomar conversación con decisor' },
  { id: 'opp-squania', companyId: 'squania', name: 'Oportunidad Squania Suite', stage: 'LOST', nextAction: 'Revisar motivo de pérdida' },
  { id: 'opp-acruxs', companyId: 'acruxs', name: 'Oportunidad Acruxs', stage: 'LOST', nextAction: 'Revisar motivo de pérdida' },
  { id: 'opp-paramuno', companyId: 'paramuno', name: 'Oportunidad Paramuno', stage: 'LOST', nextAction: 'Revisar motivo de pérdida' },
]

export const initialCrmTasks: CrmTask[] = [
  { id: 'task-celisano', companyId: 'celisano', title: 'Visita de diagnóstico', due: '21 AGO · 12:00', priority: 'A+', done: false },
  { id: 'task-manzor', companyId: 'manzor', title: 'Validar alcance y preview', due: 'Hoy', priority: 'A', done: false },
  { id: 'task-cedros', companyId: 'los-cedros', title: 'Primer contacto web/canal directo', due: 'Pendiente', priority: 'A+', done: false },
  { id: 'task-manantiales', companyId: 'manantiales', title: 'Presentar propuesta de canal directo', due: 'Pendiente', priority: 'A', done: false },
]
