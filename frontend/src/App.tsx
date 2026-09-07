import { useEffect, useMemo, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { CrmViews } from './components/CrmViews'
import { initialCompanies, initialContacts, initialCrmTasks, initialLeads, initialOpportunities, type Company, type Contact, type CrmTask, type Lead, type Opportunity, type OpportunityStage } from './data/crm'
import { priorityLeads } from './data/prospecting'
import './App.css'
import './auth.css'
import './chat.css'

type NavItem = 'Hoy' | 'Pipeline' | 'Empresas' | 'Contactos' | 'Tareas'
type ChatRole = 'user' | 'assistant'
type ActionPreview = { title: string; entityType?: string; entityName?: string; entityId?: string; details: Record<string, string> }
type PendingAction = { id: string; conversationId?: string; toolName: string; riskLevel: string; status: string; expiresAt: string; preview: ActionPreview; result?: Record<string, unknown>; failureReason?: string }
type ChatMessage = { id: string; role: ChatRole; content: string; pendingActions?: PendingAction[] }
type ConversationPayload = { messages: Array<{ id: string; role: string; content: string }> }
type AuthUser = { id: string; tenantId: string; email: string; name: string; roles: string[] }

const navItems: NavItem[] = ['Hoy', 'Pipeline', 'Empresas', 'Contactos', 'Tareas']
const conversationStorageKey = 'suture.syna.conversation-id'
const pipelineOrder: OpportunityStage[] = ['NEW', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'WON', 'LOST']
const pipelineLabels: Record<OpportunityStage, string> = { NEW: 'Nuevas', QUALIFIED: 'Calificadas', PROPOSAL: 'Propuesta', NEGOTIATION: 'Negociación', WON: 'Ganadas', LOST: 'Perdidas' }

function pendingAction(value: unknown): PendingAction {
  const source = value && typeof value === 'object' ? value as Record<string, unknown> : {}
  const rawPreview = source.preview && typeof source.preview === 'object' ? source.preview as Record<string, unknown> : {}
  const rawDetails = rawPreview.details && typeof rawPreview.details === 'object' ? rawPreview.details as Record<string, unknown> : {}
  const details = Object.fromEntries(Object.entries(rawDetails).filter(([, detail]) => typeof detail === 'string').map(([label, detail]) => [label, detail as string]))
  const expiresAt = typeof source.expiresAt === 'string' && !Number.isNaN(new Date(source.expiresAt).getTime())
    ? source.expiresAt : new Date(Date.now() + 15 * 60_000).toISOString()
  return {
    id: typeof source.id === 'string' ? source.id : `unknown-action-${Date.now()}`,
    conversationId: typeof source.conversationId === 'string' ? source.conversationId : undefined,
    toolName: typeof source.toolName === 'string' ? source.toolName : 'acción pendiente',
    riskLevel: typeof source.riskLevel === 'string' ? source.riskLevel : 'WRITE',
    status: typeof source.status === 'string' ? source.status : 'PENDING',
    expiresAt,
    preview: {
      title: typeof rawPreview.title === 'string' ? rawPreview.title : `Revisar: ${typeof source.toolName === 'string' ? source.toolName : 'acción pendiente'}`,
      entityType: typeof rawPreview.entityType === 'string' ? rawPreview.entityType : undefined,
      entityName: typeof rawPreview.entityName === 'string' ? rawPreview.entityName : undefined,
      entityId: typeof rawPreview.entityId === 'string' ? rawPreview.entityId : undefined,
      details,
    },
    result: source.result && typeof source.result === 'object' ? source.result as Record<string, unknown> : undefined,
    failureReason: typeof source.failureReason === 'string' ? source.failureReason : undefined,
  }
}

function pendingActionList(value: unknown): PendingAction[] {
  if (!Array.isArray(value)) return []
  const seen = new Set<string>()
  return value.map(pendingAction).filter((action) => {
    if (seen.has(action.id)) return false
    seen.add(action.id)
    return true
  })
}

function actionDecisionError(status: number): string {
  if (status === 401) return 'Tu sesión ya no permite aprobar esta acción. Volvé a ingresar al CRM.'
  if (status === 403) return 'El CRM rechazó la autorización de Syna para esta acción. Actualizá la pantalla y probá nuevamente.'
  if (status === 404 || status === 409) return 'Esta acción ya fue resuelta o venció. Actualizá Syna para ver su estado final.'
  if (status === 502 || status === 503) return 'Syna no está disponible para actualizar esta acción. Probá nuevamente en unos instantes.'
  return 'No se pudo actualizar esta acción. Probá nuevamente.'
}

function taskTitle(action: PendingAction): string {
  return action.preview.title.replace(/^Crear tarea:\s*/i, '').trim() || 'Tarea creada por Syna'
}

function taskDescription(action: PendingAction): string {
  const detail = action.preview.details.Descripción
  if (detail) return detail
  const entity = action.preview.entityName ?? action.preview.entityType ?? 'la entidad vinculada'
  return `Syna creará esta tarea para ${entity}.`
}

function taskIntent(action: PendingAction): string | null {
  if (action.toolName !== 'create_crm_task') return null
  const entity = action.preview.entityName ?? action.preview.entityType ?? 'la entidad vinculada'
  return `Al aprobar, Syna creará “${taskTitle(action)}” para ${entity}.`
}

function Icon({ name }: { name: string }) {
  const paths: Record<string, string> = {
    Hoy: 'M8 3H4v4m12-4h4v4M4 17v4h4m12-4v4h-4M8 12h8m-4-4v8',
    Pipeline: 'M4 19V9m8 10V4m8 15v-7',
    Empresas: 'M4 21V5h10v16M14 10h6v11M8 9h2m-2 4h2m-2 4h2M2 21h20',
    Contactos: 'M15 20v-2a5 5 0 0 0-10 0v2m13-13a4 4 0 0 1 0 8m4 5v-2a5 5 0 0 0-3-4M14 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0',
    Tareas: 'm8 12 3 3 8-9M20 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h9',
    arrow: 'M5 12h14m-5-5 5 5-5 5',
    calendar: 'M8 2v4m8-4v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14H3V6a2 2 0 0 1 2-2',
    spark: 'm12 3 2.5 6.5L21 12l-6.5 2.5L12 21l-2.5-6.5L3 12l6.5-2.5Z',
    send: 'M12 19V5m-6 6 6-6 6 6',
    close: 'm6 6 12 12M6 18 18 6',
    logout: 'M9 3H4v18h5m5-14 5 5-5 5m-5-5h10',
    chevron: 'm9 5 7 7-7 7',
  }
  return <svg className="icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={paths[name] ?? paths.Empresas} /></svg>
}

function PanelTitle({ title, count, detail }: { title: string; count?: number; detail?: string }) {
  return <div className="panel-title"><h2>{title}{count !== undefined && <span className="count-badge">{count}</span>}</h2>{detail && <span className="panel-detail">{detail}</span>}</div>
}

function MarkdownMessage({ content }: { content: string }) {
  const inline = (value: string) => value.split(/(\*\*[^*]+\*\*)/g).map((part, index) =>
    part.startsWith('**') && part.endsWith('**')
      ? <strong key={index}>{part.slice(2, -2)}</strong>
      : part,
  )
  return <>{content.split(/\r?\n/).map((line, index) => {
    const bullet = line.match(/^\s*[-*]\s+(.+)$/)
    const numbered = line.match(/^\s*(\d+)\.\s+(.+)$/)
    const heading = line.match(/^\s*#{1,6}\s+(.+)$/)
    if (bullet) return <p className="markdown-bullet" key={index}>• {inline(bullet[1])}</p>
    if (numbered) return <p className="markdown-bullet" key={index}>{numbered[1]}. {inline(numbered[2])}</p>
    if (heading) return <p className="markdown-heading" key={index}>{inline(heading[1])}</p>
    if (!line.trim()) return <br key={index} />
    return <p key={index}>{inline(line)}</p>
  })}</>
}

function ActionReviewCard({ action, now, isDeciding, onDecide }: { action: PendingAction; now: number; isDeciding: boolean; onDecide: (action: PendingAction, decision: 'approve' | 'reject') => void }) {
  const expiresIn = Math.max(0, Math.ceil((new Date(action.expiresAt).getTime() - now) / 60_000))
  const expired = action.status === 'PENDING' && new Date(action.expiresAt).getTime() <= now
  const pending = action.status === 'PENDING' && !expired
  const status = action.status === 'EXECUTED' ? 'Ejecutada' : action.status === 'REJECTED' ? 'Rechazada' : action.status === 'EXPIRED' || expired ? 'Vencida' : action.status === 'FAILED' ? 'No se pudo ejecutar' : 'Revisión requerida'
  return <section className={`action-review action-${action.status.toLowerCase()}`} aria-label={`${status}: ${action.preview.title}`}>
    <div className="action-review-heading"><span>{status}</span>{pending && <small>Vence en {expiresIn} min</small>}</div>
    <strong>{action.preview.title}</strong>
    {taskIntent(action) && <p className="action-intent">{taskIntent(action)}</p>}
    {(action.preview.entityType || action.preview.entityName) && <p className="action-entity">{action.preview.entityType ?? 'Entidad'}: {action.preview.entityName ?? action.preview.entityId}</p>}
    <dl>{Object.entries(action.preview.details ?? {}).map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl>
    {action.failureReason && <p className="action-error">{action.failureReason}</p>}
    {pending && <div className="action-review-actions"><button type="button" disabled={isDeciding} onClick={() => onDecide(action, 'approve')}>{isDeciding ? 'Actualizando…' : 'Aprobar'}</button><button type="button" className="reject" disabled={isDeciding} onClick={() => onDecide(action, 'reject')}>Rechazar</button></div>}
  </section>
}

function LoginScreen({ onAuthenticated }: { onAuthenticated: (user: AuthUser) => void }) {
  const [email, setEmail] = useState('admin@suture.local')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    try {
      const response = await fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, password }) })
      if (!response.ok) throw new Error('Revisá tu correo y contraseña.')
      onAuthenticated(await response.json() as AuthUser)
    } catch (loginError) {
      setError(loginError instanceof Error ? loginError.message : 'No fue posible iniciar sesión.')
    } finally {
      setBusy(false)
    }
  }
  return <main className="auth-page"><form className="auth-card" onSubmit={submit}><p className="auth-wordmark">SUTURE</p><h1>Ingresar al CRM</h1><p>Usá tu cuenta para acceder a tu organización y a Syna.</p><label>Correo<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="email" required /></label><label>Contraseña<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" required /></label>{error && <p className="auth-error" role="alert">{error}</p>}<button type="submit" disabled={busy}>{busy ? 'Ingresando…' : 'Ingresar'}</button></form></main>
}

function App() {
  const [activeNav, setActiveNav] = useState<NavItem>('Hoy')
  const [companies, setCompanies] = useState<Company[]>(initialCompanies)
  const [contacts, setContacts] = useState<Contact[]>(initialContacts)
  const [leads, setLeads] = useState<Lead[]>(initialLeads)
  const [opportunities, setOpportunities] = useState<Opportunity[]>(initialOpportunities)
  const [tasks, setTasks] = useState<CrmTask[]>(initialCrmTasks)
  const [selectedCompanyId, setSelectedCompanyId] = useState<string | null>(null)
  const [compact, setCompact] = useState(() => window.matchMedia('(max-width: 1099px)').matches)
  const [agentOpen, setAgentOpen] = useState(() => !window.matchMedia('(max-width: 1099px)').matches)
  const [prompt, setPrompt] = useState('')
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [pendingActions, setPendingActions] = useState<PendingAction[]>([])
  const [conversationId, setConversationId] = useState<string | null>(() => window.localStorage.getItem(conversationStorageKey))
  const [agentBusy, setAgentBusy] = useState(false)
  const [decidingActionId, setDecidingActionId] = useState<string | null>(null)
  const [now, setNow] = useState(() => Date.now())
  const [authUser, setAuthUser] = useState<AuthUser | null>(null)
  const [authReady, setAuthReady] = useState(false)
  const agentPanelRef = useRef<HTMLElement>(null)
  const agentToggleRef = useRef<HTMLButtonElement>(null)
  const chatInputRef = useRef<HTMLTextAreaElement>(null)
  useEffect(() => {
    const media = window.matchMedia('(max-width: 1099px)')
    const resize = () => { setCompact(media.matches); if (media.matches) setAgentOpen(false) }
    media.addEventListener('change', resize)
    return () => media.removeEventListener('change', resize)
  }, [])
  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 30_000)
    return () => window.clearInterval(timer)
  }, [])
  useEffect(() => {
    if (!authUser) return
    let mounted = true
    void fetch('/api/syna/actions?status=PENDING')
      .then(async (response) => response.ok ? pendingActionList(await response.json()) : [])
      .then((actions) => { if (mounted) setPendingActions(actions) })
      .catch(() => undefined)
    return () => { mounted = false }
  }, [authUser])
  useEffect(() => {
    if (!agentOpen || !authUser) return
    if (compact) chatInputRef.current?.focus()
    const previousOverflow = document.body.style.overflow
    if (compact) document.body.style.overflow = 'hidden'
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') { setAgentOpen(false); requestAnimationFrame(() => agentToggleRef.current?.focus()) }
      if (!compact || event.key !== 'Tab') return
      const controls = agentPanelRef.current?.querySelectorAll<HTMLElement>('button:not(:disabled), textarea')
      if (!controls?.length) return
      const first = controls[0], last = controls[controls.length - 1]
      if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus() }
      if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus() }
    }
    document.addEventListener('keydown', onKey)
    return () => { document.removeEventListener('keydown', onKey); document.body.style.overflow = previousOverflow }
  }, [agentOpen, compact, authUser])
  const chatHistoryRef = useRef<HTMLDivElement>(null)
  const pendingTasks = useMemo(() => tasks.filter((task) => !task.done).length, [tasks])
  const actionMessageOwners = useMemo(() => {
    const owners = new Map<string, string>()
    messages.forEach((message) => {
      if (message.role !== 'assistant') return
      message.pendingActions?.forEach((action) => owners.set(action.id, message.id))
    })
    return owners
  }, [messages])
  const pipelineSummary = useMemo(() => pipelineOrder.map((stage) => {
    const inStage = opportunities.filter((opportunity) => opportunity.stage === stage)
    const amount = inStage.reduce((total, opportunity) => total + (opportunity.value ?? 0), 0)
    return { stage, label: pipelineLabels[stage], count: inStage.length, amount: amount ? `${amount.toLocaleString('es-UY')} UYU` : '—' }
  }), [opportunities])
  const toggleTask = (id: string) => setTasks((current) => current.map((task) => task.id === id ? { ...task, done: !task.done } : task))
  const openCompany = (companyId: string) => { setSelectedCompanyId(companyId); setActiveNav('Empresas') }
  const moveOpportunity = (id: string, stage: OpportunityStage) => setOpportunities((current) => current.map((opportunity) => opportunity.id === id ? { ...opportunity, stage } : opportunity))
  useEffect(() => {
    void fetch('/api/auth/me').then(async (response) => response.ok ? response.json() as Promise<AuthUser> : null)
      .then((user) => setAuthUser(user))
      .catch(() => setAuthUser(null))
      .finally(() => setAuthReady(true))
  }, [])
  useEffect(() => {
    if (!authUser || !conversationId) return
    window.localStorage.setItem(conversationStorageKey, conversationId)
    let mounted = true
    void fetch(`/api/syna/conversations/${conversationId}`)
      .then(async (response) => {
        if (!response.ok) throw new Error('Conversation could not be restored')
        return response.json() as Promise<ConversationPayload>
      })
      .then((conversation) => {
        if (!mounted) return
        const restoredMessages = conversation.messages
          .filter((message): message is ConversationPayload['messages'][number] & { role: ChatRole } => message.role === 'user' || message.role === 'assistant')
          .map((message) => ({ id: message.id, role: message.role, content: message.content }))
        setMessages(restoredMessages)
      })
      .catch(() => undefined)
    return () => { mounted = false }
  }, [authUser, conversationId])
  useEffect(() => {
    const history = chatHistoryRef.current
    if (history?.parentElement) history.parentElement.scrollTop = history.parentElement.scrollHeight
  }, [agentBusy, messages.length])
  const askAgent = async (question: string) => {
    const value = question || prompt.trim()
    if (!value || agentBusy) return
    setPrompt('')
    setMessages((current) => [...current, { id: `local-user-${Date.now()}`, role: 'user', content: value }])
    setAgentBusy(true)
    try {
      const response = await fetch('/api/syna/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          conversationId,
          message: value,
          context: { screen: `crm/${activeNav.toLowerCase()}` },
        }),
      })
      if (!response.ok) throw new Error('Syna no pudo responder en este momento.')
      const payload: { conversationId: string, answer: string, pendingActions?: unknown } = await response.json()
      setConversationId(payload.conversationId)
      const requestedActions = pendingActionList(payload.pendingActions)
      setPendingActions((current) => [...current.filter((action) => !requestedActions.some((next) => next.id === action.id)), ...requestedActions])
      setMessages((current) => [...current, { id: `local-assistant-${Date.now()}`, role: 'assistant', content: payload.answer, pendingActions: requestedActions }])
    } catch (error) {
      setMessages((current) => [...current, { id: `local-error-${Date.now()}`, role: 'assistant', content: error instanceof Error ? error.message : 'Syna no pudo responder en este momento.' }])
    } finally {
      setAgentBusy(false)
    }
  }
  const decideAction = async (action: PendingAction, decision: 'approve' | 'reject') => {
    if (decidingActionId) return
    const updateAction = (transform: (candidate: PendingAction) => PendingAction) => {
      setPendingActions((current) => current.map((candidate) => candidate.id === action.id ? transform(candidate) : candidate))
      setMessages((current) => current.map((message) => ({ ...message, pendingActions: message.pendingActions?.map((candidate) => candidate.id === action.id ? transform(candidate) : candidate) })))
    }
    setDecidingActionId(action.id)
    updateAction((candidate) => ({ ...candidate, failureReason: undefined }))
    try {
      const response = await fetch(`/api/syna/actions/${action.id}/${decision}`, { method: 'POST' })
      if (!response.ok) throw new Error(actionDecisionError(response.status))
      const updated = pendingAction(await response.json())
      setPendingActions((current) => current.filter((candidate) => candidate.id !== action.id))
      setMessages((current) => current.map((message) => ({ ...message, pendingActions: message.pendingActions?.map((candidate) => candidate.id === updated.id ? updated : candidate) })))
      // Las vistas actuales trabajan con estado local: reflejamos la tarea aprobada inmediatamente.
      if (updated.preview.entityType?.toLowerCase().includes('oportun')) setOpportunities((current) => [...current])
      if (updated.toolName === 'create_crm_task') {
        const companyId = updated.preview.entityId ?? `syna-entity-${updated.id}`
        const companyName = updated.preview.entityName ?? updated.preview.entityType ?? 'Entidad vinculada por Syna'
        setCompanies((current) => current.some((company) => company.id === companyId)
          ? current : [...current, { id: companyId, name: companyName, industry: null, city: null, country: 'Uruguay' }])
        setTasks((current) => current.some((task) => task.id === `syna-action-${updated.id}`)
          ? current : [...current, {
            id: `syna-action-${updated.id}`,
            companyId,
            title: taskTitle(updated),
            description: taskDescription(updated),
            due: updated.preview.details.Vencimiento ?? 'Pendiente',
            priority: updated.preview.details.Prioridad ?? 'A',
            done: false,
          }])
        setSelectedCompanyId(null)
        setActiveNav('Tareas')
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'No se pudo actualizar esta acción. Probá nuevamente.'
      updateAction((candidate) => ({ ...candidate, failureReason: message }))
    } finally {
      setDecidingActionId(null)
    }
  }
  const signOut = async () => {
    await fetch('/api/auth/logout', { method: 'POST' })
    window.localStorage.removeItem(conversationStorageKey)
    setConversationId(null)
    setMessages([])
    setPendingActions([])
    setAuthUser(null)
  }

  if (!authReady) return <main className="auth-page"><p className="auth-loading">Cargando sesión…</p></main>
  if (!authUser) return <LoginScreen onAuthenticated={setAuthUser} />
  const initials = authUser.name.split(/\s+/).map((word) => word.slice(0, 1)).join('').slice(0, 2).toUpperCase()

  const activeOpportunities = opportunities.filter((opportunity) => !['WON', 'LOST'].includes(opportunity.stage)).length
  const quotedValue = opportunities.filter((opportunity) => opportunity.stage === 'PROPOSAL' && opportunity.currency === 'UYU').reduce((sum, opportunity) => sum + (opportunity.value ?? 0), 0)
  const closeAgent = () => { setAgentOpen(false); requestAnimationFrame(() => agentToggleRef.current?.focus()) }
  const navigate = (item: NavItem) => { setActiveNav(item); if (item !== 'Empresas') setSelectedCompanyId(null) }

  return <div className={`app-shell ${agentOpen ? 'agent-visible' : ''}`}>
    <a className="skip-link" href="#main-content">Saltar al contenido</a>
    <aside className="sidebar" inert={compact && agentOpen}>
      <div className="wordmark"><span className="brand-symbol" aria-hidden="true">s.</span><span>SUTURE<small>CRM comercial</small></span></div>
      <div className="nav-label">Espacio de trabajo</div>
      <nav aria-label="Navegación principal">{navItems.map((item) => <button key={item} onClick={() => navigate(item)} aria-current={activeNav === item ? 'page' : undefined} className={`nav-item ${activeNav === item ? 'selected' : ''}`}><Icon name={item} /><span>{item}</span>{item === 'Tareas' && <span className="nav-count">{pendingTasks}</span>}</button>)}</nav>
      <div className="sidebar-bottom"><span className="workspace-caption">Organización · {authUser.tenantId.slice(0, 8)}</span><button className="profile" onClick={() => void signOut()} aria-label={`Cerrar sesión de ${authUser.name}`} title="Cerrar sesión"><span className="avatar">{initials}</span><span className="profile-copy"><strong>{authUser.name}</strong><small>{authUser.roles.includes('CRM_ADMIN') ? 'Administrador' : 'Usuario CRM'}</small></span><Icon name="logout" /></button></div>
    </aside>
    <div className="main-column" inert={compact && agentOpen}>
      <header className="topbar"><div className="breadcrumb"><span>CRM</span><Icon name="chevron" /><strong>{activeNav}</strong></div><button ref={agentToggleRef} className={`agent-toggle ${agentOpen ? 'active' : ''}`} onClick={() => setAgentOpen(!agentOpen)} aria-expanded={agentOpen} aria-controls="syna-panel"><Icon name="spark" /><span>Syna</span><span className="toggle-label">Asistente</span></button></header>
      <main className="workspace" id="main-content" tabIndex={-1}>
      {activeNav === 'Hoy' ? <>
        <header className="page-heading"><div><h1>Lo importante, hoy.</h1><p className="page-description">Cada conversación, un próximo paso.</p></div><button className="secondary-action" onClick={() => navigate('Pipeline')}>Abrir pipeline<Icon name="arrow" /></button></header>
        <div className="overview-line" aria-label="Resumen comercial"><button onClick={() => navigate('Pipeline')}><strong>{activeOpportunities}</strong> oportunidades activas<Icon name="arrow" /></button><button onClick={() => navigate('Empresas')}><strong>{companies.length}</strong> empresas<Icon name="arrow" /></button><button onClick={() => navigate('Tareas')}><strong>{pendingTasks}</strong> tareas pendientes<Icon name="arrow" /></button></div>
        <div className="dashboard-grid">
          <section className="panel priorities" aria-label="Prioridades comerciales">
            <PanelTitle title="Requieren tu atención" count={priorityLeads.length} detail="Próximas acciones" />
            <ol className="priority-list">{priorityLeads.map((item) => <li key={item.number}><button className="priority-row" onClick={() => { const company = companies.find((candidate) => candidate.name === item.name || candidate.name.startsWith(item.name)); if (company) openCompany(company.id) }} aria-label={`Abrir ${item.name}`}><span className="priority-number">{item.number}</span><span className="priority-copy"><span className="row-title"><strong>{item.name}</strong><span className={`level ${item.temperature.toLowerCase()}`}><span>{item.priority}</span>{item.temperature === 'HOT' ? 'Hot' : 'Warm'}</span></span><span className="priority-action">{item.action}</span><span className="meta"><span>{item.sector}</span>{item.person && <span>{item.person}</span>}<span><Icon name="calendar" />{item.when}</span></span></span><Icon name="arrow" /></button></li>)}</ol>
            <button className="panel-footer" onClick={() => navigate('Pipeline')}>Ver oportunidades<Icon name="arrow" /></button>
          </section>
          <section className="panel pipeline" aria-label="Resumen del pipeline">
            <PanelTitle title="Pipeline" detail={`${opportunities.length} oportunidades`} />
            <div className="pipeline-quotation"><p className="tiny-label">Valor en propuesta</p><div className="pipeline-value">{quotedValue.toLocaleString('es-UY')} <small>UYU</small></div></div>
            <div className="stages">{pipelineSummary.map(({ stage, label, count, amount }) => <div className={`stage ${stage === 'PROPOSAL' ? 'current' : ''}`} key={stage}><div className="stage-heading"><span>{label}</span><span className="stage-count">{count}</span></div><div className="stage-track" aria-hidden="true"><span style={{ width: `${opportunities.length ? count / opportunities.length * 100 : 0}%` }} /></div><span className="stage-amount">{amount}</span></div>)}</div>
            <button className="panel-footer" onClick={() => navigate('Pipeline')}>Ver pipeline completo<Icon name="arrow" /></button>
          </section>
          <section className="panel tasks" aria-label="Tareas próximas">
            <PanelTitle title="Tareas próximas" count={pendingTasks} detail={`${tasks.filter((task) => task.done).length} completadas`} />
            {tasks.length ? <div className="task-table"><table><caption className="sr-only">Tareas próximas: empresa, vencimiento y prioridad</caption><thead><tr><th scope="col"><span className="sr-only">Estado</span></th><th scope="col">Tarea</th><th scope="col">Empresa</th><th scope="col">Vencimiento</th><th scope="col">Prioridad</th></tr></thead><tbody>{tasks.map((task) => <tr className={`task-row ${task.done ? 'completed' : ''}`} key={task.id}><td><input type="checkbox" className="check" aria-label={`Completar ${task.title}`} checked={task.done} onChange={() => toggleTask(task.id)} /></td><th scope="row">{task.title}</th><td className="task-company"><button className="company-link" onClick={() => openCompany(task.companyId)}>{companies.find((company) => company.id === task.companyId)?.name}</button></td><td className="task-due"><span><Icon name="calendar" />{task.due}</span></td><td className="task-priority"><span className="tag">{task.priority}</span></td></tr>)}</tbody></table></div> : <p className="empty-state">No hay tareas próximas. Podés crear una desde Tareas.</p>}
            <button className="panel-footer" onClick={() => navigate('Tareas')}>Ver tareas<span>{pendingTasks} pendientes<Icon name="arrow" /></span></button>
          </section>
        </div>
      </> : <CrmViews view={activeNav} companies={companies} contacts={contacts} leads={leads} opportunities={opportunities} tasks={tasks} selectedCompanyId={selectedCompanyId} onOpenCompany={openCompany} onBack={() => setSelectedCompanyId(null)} onCreateCompany={(company) => setCompanies((current) => [...current, company])} onCreateContact={(contact) => setContacts((current) => [...current, contact])} onCreateLead={(lead) => setLeads((current) => [...current, lead])} onCreateOpportunity={(opportunity) => setOpportunities((current) => [...current, opportunity])} onMoveOpportunity={moveOpportunity} onCreateTask={(task) => setTasks((current) => [...current, task])} onToggleTask={toggleTask} />}
      </main>
    </div>
    {compact && agentOpen && <div className="agent-backdrop" onClick={closeAgent} aria-hidden="true" />}
    {agentOpen && <aside id="syna-panel" ref={agentPanelRef} className={`agent-panel ${messages.length ? 'has-history' : ''}`} role={compact ? 'dialog' : 'complementary'} aria-modal={compact ? true : undefined} aria-labelledby="syna-title">
      <header className="agent-header"><div className="agent-identity"><span className="agent-mark" aria-hidden="true"><Icon name="spark" /></span><div><h2 id="syna-title">Syna</h2><p>Asistente comercial</p></div></div><button className="icon-button" onClick={closeAgent} aria-label="Cerrar Syna"><Icon name="close" /></button></header>
      <div className="agent-context"><span>Contexto actual</span><strong>{activeNav}</strong></div>
      <div className="agent-body">
        {!messages.length && <div className="agent-welcome"><h3>¿Por dónde<br />empezamos?</h3><p>Consultá oportunidades, prepará tu próxima conversación o revisá tus pendientes.</p></div>}
        <div className="suggestions" aria-label="Consultas sugeridas">{['¿Qué oportunidades necesitan atención?', 'Muéstrame el pipeline de este mes', 'Resumir últimas interacciones con Celisano', '¿Qué tareas tengo pendientes hoy?'].map((question, index) => <button disabled={agentBusy} key={question} onClick={() => void askAgent(question)}><Icon name={index === 0 ? 'spark' : index === 1 ? 'Pipeline' : index === 2 ? 'Contactos' : 'Tareas'} /><span>{question}</span><Icon name="arrow" /></button>)}</div>
        {(messages.length > 0 || pendingActions.length > 0 || agentBusy) && <div className="chat-history" role="log" aria-live="polite" aria-label="Conversación con Syna" ref={chatHistoryRef}>{messages.map((message) => <article className={`chat-turn ${message.role}`} key={message.id}><span>{message.role === 'user' ? 'Vos' : 'Syna'}</span><div className="chat-message"><MarkdownMessage content={message.content} /></div>{message.role === 'assistant' && message.pendingActions?.filter((action) => actionMessageOwners.get(action.id) === message.id).map((action) => <ActionReviewCard key={action.id} action={action} now={now} isDeciding={decidingActionId === action.id} onDecide={decideAction} />)}</article>)}{pendingActions.filter((action) => !actionMessageOwners.has(action.id)).map((action) => <article className="chat-turn assistant" key={`pending-${action.id}`}><span>Syna</span><ActionReviewCard action={action} now={now} isDeciding={decidingActionId === action.id} onDecide={decideAction} /></article>)}{agentBusy && <article className="chat-turn assistant"><span>Syna</span><div className="chat-message typing">Consultando tu contexto…</div></article>}</div>}
      </div>
      <div className="agent-composer"><form className="agent-input" onSubmit={(event) => { event.preventDefault(); void askAgent('') }}><label className="sr-only" htmlFor="syna-question">Tu consulta para Syna</label><textarea id="syna-question" ref={chatInputRef} value={prompt} onChange={(event) => setPrompt(event.target.value)} placeholder="Preguntale a Syna…" rows={3} /><div className="composer-bottom"><span>Contexto del CRM</span><button type="submit" aria-label="Enviar consulta" disabled={agentBusy || !prompt.trim()}><Icon name="send" /></button></div></form><p className="agent-note">Syna puede cometer errores.<br />Verificá la información importante.</p></div>
    </aside>}
  </div>
}

export default App
