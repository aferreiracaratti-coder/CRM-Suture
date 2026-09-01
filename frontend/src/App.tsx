import { useMemo, useState } from 'react'
import { CrmViews } from './components/CrmViews'
import { initialCompanies, initialContacts, initialCrmTasks, initialLeads, initialOpportunities, type Company, type Contact, type CrmTask, type Lead, type Opportunity, type OpportunityStage } from './data/crm'
import { pipelineStages, priorityLeads } from './data/prospecting'
import './App.css'

type NavItem = 'Hoy' | 'Pipeline' | 'Empresas' | 'Contactos' | 'Tareas'

const navItems: NavItem[] = ['Hoy', 'Pipeline', 'Empresas', 'Contactos', 'Tareas']

function Icon({ name }: { name: string }) {
  const glyphs: Record<string, string> = { Hoy: '◎', Pipeline: '▥', Empresas: '⌂', Contactos: '♙', Tareas: '☑', arrow: '→', calendar: '▣', clock: '◷', spark: '✦', expand: '↗', send: '↑', chevron: '⌄', person: '♙', building: '▥' }
  return <span className="icon" aria-hidden="true">{glyphs[name] ?? '·'}</span>
}

function PanelTitle({ index, title, action }: { index: string; title: string; action?: string }) {
  return <div className="panel-title"><span>{index}&nbsp;&nbsp;{title}</span>{action && <button>{action} <Icon name="chevron" /></button>}</div>
}

function App() {
  const [activeNav, setActiveNav] = useState<NavItem>('Hoy')
  const [companies, setCompanies] = useState<Company[]>(initialCompanies)
  const [contacts, setContacts] = useState<Contact[]>(initialContacts)
  const [leads, setLeads] = useState<Lead[]>(initialLeads)
  const [opportunities, setOpportunities] = useState<Opportunity[]>(initialOpportunities)
  const [tasks, setTasks] = useState<CrmTask[]>(initialCrmTasks)
  const [selectedCompanyId, setSelectedCompanyId] = useState<string | null>(null)
  const [agentOpen, setAgentOpen] = useState(true)
  const [prompt, setPrompt] = useState('')
  const [agentReply, setAgentReply] = useState('')
  const pendingTasks = useMemo(() => tasks.filter((task) => !task.done).length, [tasks])
  const toggleTask = (id: string) => setTasks((current) => current.map((task) => task.id === id ? { ...task, done: !task.done } : task))
  const openCompany = (companyId: string) => { setSelectedCompanyId(companyId); setActiveNav('Empresas') }
  const moveOpportunity = (id: string, stage: OpportunityStage) => setOpportunities((current) => current.map((opportunity) => opportunity.id === id ? { ...opportunity, stage } : opportunity))
  const askAgent = (question: string) => {
    const value = question || prompt.trim()
    if (!value) return
    setPrompt('')
    setAgentReply('La prioridad es Celisano: tiene visita de diagnóstico el 21 de agosto y no se debe cotizar antes. Luego validá el presupuesto de Estudio Manzor y hacé el primer contacto con Hotel Los Cedros.')
  }

  return <main className="app-shell">
    <aside className="sidebar"><div className="wordmark">SUTURE</div><nav aria-label="Navegación principal">{navItems.map((item) => <button key={item} onClick={() => { setActiveNav(item); if (item !== 'Empresas') setSelectedCompanyId(null) }} className={`nav-item ${activeNav === item ? 'selected' : ''}`}><Icon name={item} />{item}</button>)}</nav><div className="profile"><span className="avatar">AF</span><span><strong>Angel Ferreira</strong><small>Suture Studio</small></span><Icon name="chevron" /></div></aside>
    <section className="workspace">
      {activeNav === 'Hoy' ? <><header className="page-heading"><p className="screen-label"><i /> QUÉ REQUIERE ATENCIÓN HOY</p><h1>Buenos días, Angel</h1><p className="mobile-section">{activeNav}</p></header><div className="dashboard-grid">
        <section className="panel priorities"><PanelTitle index="01" title="PRIORIDADES" action="Orden: Prioridad" />{priorityLeads.map((item) => <article className="priority-row" key={item.number}><span className="priority-number">{item.number}</span><div className="priority-copy"><div className="row-title"><h2>{item.name}</h2><span className={`level ${item.priority.toLowerCase()}`}>{item.priority} · {item.temperature}</span></div><p>{item.action}</p><div className="meta"><span><Icon name="building" />{item.sector}</span>{item.person && <span><Icon name="person" />{item.person}</span>}<span><Icon name="calendar" />{item.when}</span></div></div><button className="row-arrow" aria-label={`Abrir ${item.name}`} onClick={() => { const company = companies.find((candidate) => candidate.name === item.name || candidate.name.startsWith(item.name)); if (company) openCompany(company.id) }}><Icon name="arrow" /></button></article>)}<button className="panel-footer" onClick={() => setActiveNav('Pipeline')}>Ver todas las prioridades (14)<Icon name="arrow" /></button></section>
        <section className="panel pipeline"><PanelTitle index="02" title="PIPELINE" action="Actual" /><p className="tiny-label">VALOR COTIZADO</p><div className="pipeline-value">$45.000 <small>UYU</small></div><p className="opportunity-count">{opportunities.filter((opportunity) => !['WON', 'LOST'].includes(opportunity.stage)).length} oportunidades activas</p><div className="stages">{pipelineStages.map(([name, count, amount], index) => <div className={`stage ${index === 2 ? 'current' : ''}`} key={name}><span className="stage-dot" /><div><strong>{name}</strong><small>{count}</small></div><b>{amount}</b></div>)}</div><button className="panel-footer" onClick={() => setActiveNav('Pipeline')}>Ver pipeline completo<Icon name="arrow" /></button></section>
        <section className="panel tasks"><PanelTitle index="03" title="TAREAS PRÓXIMAS" /><div className="task-table">{tasks.map((task) => <div className={`task-row ${task.done ? 'completed' : ''}`} key={task.id}><button className="check" aria-label={`Marcar ${task.title}`} onClick={() => toggleTask(task.id)}>{task.done && '✓'}</button><strong>{task.title}</strong><button className="company-link" onClick={() => openCompany(task.companyId)}>{companies.find((company) => company.id === task.companyId)?.name}</button><span><Icon name="calendar" />{task.due}</span><span><Icon name="clock" />—</span><em className={`tag ${task.priority.toLowerCase()}`}>{task.priority}</em></div>)}</div><button className="panel-footer" onClick={() => setActiveNav('Tareas')}>Tareas pendientes ({pendingTasks})<Icon name="arrow" /></button></section>
      </div></> : <CrmViews view={activeNav} companies={companies} contacts={contacts} leads={leads} opportunities={opportunities} tasks={tasks} selectedCompanyId={selectedCompanyId} onOpenCompany={openCompany} onBack={() => setSelectedCompanyId(null)} onCreateCompany={(company) => setCompanies((current) => [...current, company])} onCreateContact={(contact) => setContacts((current) => [...current, contact])} onCreateLead={(lead) => setLeads((current) => [...current, lead])} onCreateOpportunity={(opportunity) => setOpportunities((current) => [...current, opportunity])} onMoveOpportunity={moveOpportunity} onCreateTask={(task) => setTasks((current) => [...current, task])} onToggleTask={toggleTask} />}
    </section>
    <aside className={`agent-panel ${agentOpen ? '' : 'collapsed'}`}><header><div><h2>Syna</h2><p><i /> En línea</p></div><button onClick={() => setAgentOpen(!agentOpen)} aria-label="Abrir o cerrar agente"><Icon name="expand" /></button></header>{agentOpen && <><div className="agent-orb">S</div><p className="agent-intro">Soy Syna, tu agente de IA.<br />¿En qué puedo ayudarte hoy?</p><div className="suggestions">{['¿Qué oportunidades necesitan atención?', 'Muéstrame el pipeline de este mes', 'Resumir últimas interacciones con Celisano', '¿Qué tareas tengo pendientes hoy?'].map((question, index) => <button key={question} onClick={() => askAgent(question)}><Icon name={index === 0 ? 'spark' : index === 1 ? 'Pipeline' : index === 2 ? 'person' : 'Tareas'} />{question}</button>)}</div>{agentReply && <p className="agent-reply">{agentReply}</p>}<form className="agent-input" onSubmit={(event) => { event.preventDefault(); askAgent('') }}><textarea value={prompt} onChange={(event) => setPrompt(event.target.value)} placeholder="Escribe tu pregunta..." rows={2} /><button type="submit" aria-label="Enviar"><Icon name="send" /></button></form><p className="agent-note">Syna puede cometer errores.<br />Verifica la información importante.</p></>}</aside>
  </main>
}

export default App
