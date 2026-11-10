import { api, post, patch, put, remove, authToken, clearSession, savedUser, saveSession } from "./api.js";

const app = document.querySelector("#app");
const homeMarkup = app.innerHTML;
const dialog = document.querySelector("#app-dialog");
const dialogContent = document.querySelector("#dialog-content");
const toastRegion = document.querySelector("#toast-region");
const state = { user: savedUser(), properties: [], page: 0, pages: 0, filters: {}, sort: "latest", compare: [] };
const money = value => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(value || 0);
const escape = value => String(value ?? "").replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[char]));
const localImage = path => path?.startsWith("/assets/images/") ? path : "/assets/images/properties/villa-01.jpg";
const propertyPrice = property => `${money(property.price)}${property.listingType === "For Rent" ? '<small> / month</small>' : ""}`;

function toast(message, error = false) {
  const item = document.createElement("div");
  item.className = `toast${error ? " error" : ""}`;
  item.textContent = message;
  toastRegion.append(item);
  window.setTimeout(() => item.remove(), 4200);
}

function showDialog(content, extraClass = "") {
  dialogContent.innerHTML = content;
  dialog.className = `app-dialog ${extraClass}`;
  dialog.showModal();
}

function closeDialog() {
  if (dialog.open) dialog.close();
  dialogContent.innerHTML = "";
}

function setHeader() {
  const header = document.querySelector("#header-actions");
  header.innerHTML = state.user
    ? `<button class="button button-quiet" data-action="dashboard">${escape(state.user.fullName.split(" ")[0])}’s workspace</button><button class="button button-dark button-small" data-action="logout">Sign out</button>`
    : '<button class="button button-quiet" data-action="login">Log in</button><button class="button button-dark button-small" data-action="register">Get started <span aria-hidden="true">↗</span></button>';
}

function propertyCard(property) {
  const favorite = state.user?.role === "BUYER" && state.favoriteIds?.has(property.id);
  const compared = state.compare.some(item => item.id === property.id);
  return `<article class="property-card">
    <div class="property-image-wrap">
      <img class="property-image" src="${escape(localImage(property.imageUrl))}" alt="${escape(property.title)}" loading="lazy">
      <span class="property-tag">${escape(property.listingType)}</span>${property.featured ? '<span class="property-tag featured-tag">ESTATEPRO PICK</span>' : ""}
      <button class="icon-action" data-action="favorite" data-id="${property.id}" aria-label="${favorite ? "Remove from" : "Add to"} favourites">${favorite ? "♥" : "♡"}</button>
      <button class="icon-action" style="top:52px" data-action="compare" data-id="${property.id}" aria-label="${compared ? "Remove from" : "Add to"} comparison">${compared ? "−" : "⇄"}</button>
    </div>
    <div class="property-card-body">
      <div class="property-place">${escape(property.locality)} <span aria-hidden="true">·</span> ${escape(property.city)}</div>
      <h3>${escape(property.title)}</h3><p class="property-address">${escape(property.propertyType)} · ${escape(property.city)}</p>
      <div class="property-meta"><span>⌂ ${property.bedrooms ?? "—"} beds</span><span>◫ ${property.bathrooms ?? "—"} baths</span><span>↔ ${Number(property.areaSqft || 0).toLocaleString("en-IN")} sq.ft.</span></div>
      <div class="property-price-row"><strong class="property-price">${propertyPrice(property)}</strong>
        <button class="card-arrow" data-action="details" data-id="${property.id}" aria-label="View ${escape(property.title)}">↗</button></div>
    </div></article>`;
}

function queryString() {
  const query = new URLSearchParams({ page: String(state.page), size: "9", sort: state.sort });
  for (const [key, value] of Object.entries(state.filters)) if (value !== "" && value != null) query.set(key, value);
  return query.toString();
}

async function loadProperties() {
  const grid = document.querySelector("#property-grid");
  if (!grid) return;
  grid.innerHTML = '<div class="loading-state"><span class="spinner"></span> Finding exceptional spaces…</div>';
  try {
    const result = await api(`/properties?${queryString()}`);
    state.properties = result.content || [];
    state.pages = result.totalPages || 0;
    grid.innerHTML = state.properties.length
      ? state.properties.map(propertyCard).join("")
      : '<div class="empty-state">No homes found just yet. Try adjusting your search or clearing the filters.</div>';
    document.querySelector("#results-label").textContent = `${result.totalElements || 0} thoughtfully selected homes`;
    renderPagination();
    renderCompareDock();
  } catch (error) {
    grid.innerHTML = `<div class="empty-state">We couldn’t load the collection. ${escape(error.message)} <button class="table-action" data-action="retry">Try again</button></div>`;
  }
}

async function loadAgents() {
  const grid = document.querySelector("#agent-grid");
  if (!grid) return;
  try {
    const agents = await api("/agents");
    grid.innerHTML = agents.length ? agents.map(agent => `<article class="agent-card">
      <img src="${escape(agent.portraitUrl)}" alt="${escape(agent.fullName)}, EstatePro property advisor" loading="lazy">
      <h3>${escape(agent.fullName)}</h3><p>EstatePro property advisor</p><span>Personal guidance, every step</span>
    </article>`).join("") : '<div class="empty-state">Our local advisory team is growing. Register to hear from EstatePro when a specialist is available in your area.</div>';
  } catch (error) {
    grid.innerHTML = `<div class="empty-state">Our advisors could not be loaded. ${escape(error.message)}</div>`;
  }
}

function renderPagination() {
  const target = document.querySelector("#pagination");
  if (!target || state.pages < 2) { if (target) target.innerHTML = ""; return; }
  const start = Math.max(0, Math.min(state.page - 2, state.pages - 5));
  target.innerHTML = Array.from({ length: Math.min(5, state.pages) }, (_, index) => {
    const page = start + index;
    return `<button class="page-button${page === state.page ? " active" : ""}" data-action="page" data-page="${page}">${page + 1}</button>`;
  }).join("");
}

function renderCompareDock() {
  document.querySelector(".compare-dock")?.remove();
  if (!state.compare.length) return;
  const dock = document.createElement("div");
  dock.className = "compare-dock";
  dock.innerHTML = `<span>${state.compare.length} homes selected</span><button class="button button-gold" data-action="show-compare">Compare homes</button><button class="table-action" data-action="clear-compare">Clear</button>`;
  document.body.append(dock);
}

async function loadFavorites() {
  state.favoriteIds = new Set();
  if (state.user?.role !== "BUYER") return;
  try {
    const favorites = await api("/favorites");
    state.favoriteIds = new Set(favorites.map(property => property.id));
  } catch (error) {
    state.favoriteIds = new Set();
    toast(`Saved homes could not be loaded: ${error.message}`, true);
  }
}

function syncHome() {
  app.innerHTML = homeMarkup;
  setHeader();
  const form = document.querySelector("#search-form");
  if (form) for (const [key, value] of Object.entries(state.filters)) if (form.elements[key]) form.elements[key].value = value;
  const quick = document.querySelector("#quick-search");
  if (quick) quick.value = state.filters.q || "";
  const sort = document.querySelector("#sort-select");
  if (sort) sort.value = state.sort;
  loadProperties();
  loadAgents();
}

function authDialog(isRegister = false) {
  showDialog(`<div class="dialog-shell auth-shell">
    <button class="dialog-close" data-action="close-dialog" aria-label="Close">×</button>
    <p class="eyebrow"><span class="eyebrow-rule"></span> WELCOME TO ESTATEPRO</p>
    <h2 class="dialog-heading">${isRegister ? "A new chapter starts here." : "Welcome back."}</h2>
    <p class="dialog-subtitle">${isRegister ? "Create your account to save homes and connect with our property team." : "Sign in to pick up where you left off."}</p>
    <form id="auth-form" data-mode="${isRegister ? "register" : "login"}">
      ${isRegister ? `<div class="form-field"><label for="auth-name">Full name</label><input id="auth-name" name="fullName" required maxlength="120" autocomplete="name"></div>` : ""}
      <div class="form-field"><label for="auth-email">Email address</label><input id="auth-email" name="email" type="email" required autocomplete="email"></div>
      <div class="form-field" style="margin-top:13px"><label for="auth-password">Password</label><input id="auth-password" name="password" type="password" required minlength="${isRegister ? 10 : 1}" autocomplete="${isRegister ? "new-password" : "current-password"}"></div>
      ${isRegister ? `<div class="form-field" style="margin-top:13px"><label for="auth-role">I’m here as a</label><select name="role" id="auth-role"><option value="BUYER">Buyer / tenant</option><option value="SELLER">Seller / owner</option></select></div>` : ""}
      <p class="form-error" id="auth-error" hidden></p>
      <div class="form-actions"><button class="button button-dark" type="submit">${isRegister ? "Create account" : "Sign in"} <span>↗</span></button></div>
    </form>
    <p class="dialog-subtitle" style="margin:18px 0 0">${isRegister ? "Already with us?" : "New to EstatePro?"}
      <button class="auth-switch" data-action="${isRegister ? "login" : "register"}">${isRegister ? "Sign in" : "Create an account"}</button></p>
  </div>`, "auth-dialog");
}

function detailDialog(property) {
  showDialog(`<div><button class="dialog-close" data-action="close-dialog" aria-label="Close">×</button>
    <img class="detail-cover" src="${escape(localImage(property.imageUrl))}" alt="${escape(property.title)}">
    <div class="detail-content"><div class="detail-location">${escape(property.locality)} · ${escape(property.city)} &nbsp; / &nbsp; ${escape(property.propertyType)}</div>
      <div class="detail-title-row"><h2>${escape(property.title)}</h2><strong class="detail-price">${propertyPrice(property)}</strong></div>
      <div class="detail-meta"><span>${property.bedrooms ?? "—"} bedrooms</span><span>${property.bathrooms ?? "—"} bathrooms</span><span>${Number(property.areaSqft || 0).toLocaleString("en-IN")} sq.ft.</span><span>${escape(property.listingType)}</span></div>
      <p class="detail-description">${escape(property.description)}</p>
      <p class="detail-description">A thoughtful address in ${escape(property.locality)}, close to the neighbourhood essentials and local connections that make a place feel like home.</p>
      <div class="detail-actions"><button class="button button-dark" data-action="enquire" data-id="${property.id}">Contact an advisor</button>
        <button class="button button-gold" data-action="visit" data-id="${property.id}">Schedule a visit</button>
        <button class="button button-quiet" data-action="book" data-id="${property.id}">Request to book</button></div>
    </div></div>`, "dialog-property");
}

async function openProperty(id) {
  const property = state.properties.find(item => item.id === id);
  try { detailDialog(property || await api(`/properties/${id}`)); }
  catch (error) { toast(error.message, true); }
}

function enquiryDialog(propertyId) {
  if (!state.user) { closeDialog(); authDialog(); toast("Sign in to contact the property team."); return; }
  if (state.user.role !== "BUYER") { toast("Enquiries are available to buyer accounts.", true); return; }
  showDialog(`<div class="dialog-shell"><button class="dialog-close" data-action="close-dialog">×</button><p class="eyebrow"><span class="eyebrow-rule"></span> START A CONVERSATION</p><h2 class="dialog-heading">What would you like to know?</h2>
    <p class="dialog-subtitle">A local advisor will follow up about this home.</p>
    <form id="enquiry-form" data-property="${propertyId}"><div class="form-field"><label for="enquiry-message">Your message</label><textarea id="enquiry-message" name="message" required maxlength="2000" placeholder="I’d love to know more about…"></textarea></div><p class="form-error" hidden></p><div class="form-actions"><button class="button button-dark" type="submit">Send enquiry <span>↗</span></button></div></form></div>`);
}

function visitDialog(propertyId) {
  if (!state.user) { closeDialog(); authDialog(); toast("Sign in to request a property visit."); return; }
  if (state.user.role !== "BUYER") { toast("Visit requests are available to buyer accounts.", true); return; }
  const date = new Date(Date.now() + 86400000).toISOString().slice(0, 10);
  showDialog(`<div class="dialog-shell"><button class="dialog-close" data-action="close-dialog">×</button><p class="eyebrow"><span class="eyebrow-rule"></span> SEE IT FOR YOURSELF</p><h2 class="dialog-heading">Arrange a private viewing.</h2><p class="dialog-subtitle">Choose a time between 9:00 and 17:30. Your advisor will confirm your visit.</p>
    <form id="visit-form" data-property="${propertyId}"><div class="form-grid"><div class="form-field"><label for="visit-date">Preferred date</label><input id="visit-date" name="date" type="date" min="${date}" required></div>
    <div class="form-field"><label for="visit-time">Time</label><select name="time" id="visit-time" required>${Array.from({ length: 18 }, (_, i) => { const minutes = 9 * 60 + i * 30; return `<option value="${String(Math.floor(minutes / 60)).padStart(2, "0")}:${String(minutes % 60).padStart(2, "0")}">${String(Math.floor(minutes / 60)).padStart(2, "0")}:${String(minutes % 60).padStart(2, "0")}</option>`; }).join("")}</select></div></div><p class="form-error" hidden></p><div class="form-actions"><button class="button button-dark" type="submit">Request a visit</button></div></form></div>`);
}

function listingForm(property = null) {
  const values = property || {};
  return `<section class="panel dashboard-form"><div class="panel-heading"><div><h2>${property ? "Update your property" : "Tell us about your property"}</h2><p>Every new or edited listing is reviewed before it goes live.</p></div></div>
    <form id="property-form" data-id="${property?.id || ""}">
      <div class="form-grid">
        <div class="form-field full"><label>Property title</label><input name="title" required maxlength="150" value="${escape(values.title || "")}" placeholder="A name that captures the home"></div>
        <div class="form-field full"><label>Description</label><textarea name="description" required maxlength="5000" placeholder="Tell prospective buyers what makes this home special.">${escape(values.description || "")}</textarea></div>
        <div class="form-field"><label>Property type</label><select name="propertyType" required>${["Apartment","Independent house","Villa","Commercial office","Shop","Warehouse","Penthouse","Plot"].map(type => `<option ${values.propertyType === type ? "selected" : ""}>${type}</option>`).join("")}</select></div>
        <div class="form-field"><label>Listing type</label><select name="listingType" required>${["For Sale","For Rent","For Lease"].map(type => `<option ${values.listingType === type ? "selected" : ""}>${type}</option>`).join("")}</select></div>
        <div class="form-field"><label>City</label><input name="city" required value="${escape(values.city || "")}" placeholder="Chennai"></div>
        <div class="form-field"><label>Neighbourhood</label><input name="locality" required value="${escape(values.locality || "")}" placeholder="Adyar"></div>
        <div class="form-field"><label>Price (INR)</label><input name="price" type="number" required min="1" step="1" value="${escape(values.price || "")}"></div>
        <div class="form-field"><label>Built-up area (sq.ft.)</label><input name="areaSqft" type="number" required min="1" value="${escape(values.areaSqft || "")}"></div>
        <div class="form-field"><label>Bedrooms</label><input name="bedrooms" type="number" min="0" max="20" value="${escape(values.bedrooms ?? 2)}"></div>
        <div class="form-field"><label>Bathrooms</label><input name="bathrooms" type="number" min="0" max="20" value="${escape(values.bathrooms ?? 2)}"></div>
        <div class="form-field full"><label>Property photography</label><select name="imageUrl">${["villa-01.jpg","villa-02.jpg","villa-03.jpg","villa-04.jpg","villa-05.jpg","villa-06.jpg","villa-07.jpg","apartment-01.jpg","apartment-02.jpg","apartment-03.jpg","apartment-04.jpg","apartment-05.jpg","interior-01.jpg","interior-02.jpg","interior-03.jpg","interior-04.jpg","interior-05.jpg","commercial-01.jpg","commercial-02.jpg","commercial-03.jpg"].map(name => `<option value="/assets/images/properties/${name}" ${values.imageUrl?.endsWith(name) ? "selected" : ""}>${name.replace(".jpg","").replaceAll("-"," ")}</option>`).join("")}</select></div>
        <div class="form-field full"><label>Address (optional)</label><input name="address" maxlength="250" value="${escape(values.address || "")}" placeholder="Street address"></div>
      </div>
      <p class="form-error" hidden></p><div class="form-actions"><button class="button button-dark" type="submit">${property ? "Save changes" : "Submit for review"} <span>↗</span></button>${property ? '<button class="button button-quiet" type="button" data-action="cancel-edit">Cancel</button>' : ""}</div>
    </form></section>`;
}

function statsCards(stats) {
  return `<div class="stats-grid">${Object.entries(stats || {}).map(([key, value]) => `<article class="stat-card"><span>${escape(key.replaceAll(/([A-Z])/g, " $1"))}</span><strong>${Number(value).toLocaleString("en-IN")}</strong></article>`).join("")}</div>`;
}

function propertyManagementList(properties, allowModeration = false) {
  if (!properties?.length) return '<div class="empty-state">Nothing to show here yet.</div>';
  return `<div class="dashboard-property-list">${properties.map(property => `<article class="dashboard-property">
    <img src="${escape(localImage(property.imageUrl))}" alt="" loading="lazy"><div><strong>${escape(property.title)}</strong><small>${escape(property.locality)}, ${escape(property.city)} · ${money(property.price)}</small><span class="status-pill ${escape(property.status?.toLowerCase())}">${escape(property.status)}</span></div>
    <div class="dashboard-property-actions">${allowModeration && property.status === "PENDING" ? `<button class="table-action" data-action="moderate" data-status="PUBLISHED" data-id="${property.id}">Approve</button><button class="table-action" data-action="moderate" data-status="REJECTED" data-id="${property.id}">Reject</button>` : ""}
      ${!allowModeration && ["SELLER","AGENT"].includes(state.user?.role) && property.status !== "ARCHIVED" ? `<button class="table-action" data-action="edit-property" data-id="${property.id}">Edit</button><button class="table-action" data-action="archive-property" data-id="${property.id}">Archive</button>` : ""}
      ${!allowModeration && state.user?.role === "BUYER" ? `<button class="table-action" data-action="details" data-id="${property.id}">View home</button>` : ""}
    </div></article>`).join("")}</div>`;
}

function table(rows, columns) {
  if (!rows?.length) return '<div class="empty-state">Nothing to show here yet.</div>';
  return `<div class="dashboard-table-wrap"><table class="dashboard-table"><thead><tr>${columns.map(column => `<th>${escape(column.label)}</th>`).join("")}</tr></thead><tbody>${rows.map(row => `<tr>${columns.map(column => `<td>${column.render ? column.render(row) : escape(row[column.key] ?? "—")}</td>`).join("")}</tr>`).join("")}</tbody></table></div>`;
}

function renderEnquiries(rows) {
  const statuses = ["NEW","CONTACTED","INTERESTED","VISIT_SCHEDULED","NEGOTIATION","CONVERTED","LOST"];
  return table(rows, [
    { label: "Property", render: row => `<strong>${escape(row.propertyTitle)}</strong>` },
    { label: "Customer", render: row => `${escape(row.buyerName)}<br><small>${escape(row.buyerEmail)}</small>` },
    { label: "Message", render: row => escape(row.message.slice(0, 70)) },
    { label: "Status", render: row => `<select class="table-status" aria-label="Update lead status" data-enquiry="${row.id}">${statuses.map(s => `<option ${row.status === s ? "selected" : ""}>${s}</option>`).join("")}</select>` },
    { label: "Action", render: row => `<button class="table-action" data-action="save-enquiry" data-id="${row.id}">Update</button>` }
  ]);
}

function renderAppointments(rows) {
  return table(rows, [
    { label: "Property", render: row => `<strong>${escape(row.propertyTitle)}</strong>` },
    { label: "Customer", key: "buyerName" },
    { label: "Requested for", render: row => `${escape(row.date)} · ${escape(row.time?.slice(0,5))}` },
    { label: "Status", render: row => `<span class="status-pill ${escape(row.status?.toLowerCase())}">${escape(row.status)}</span>` },
    { label: "Action", render: row => row.status === "REQUESTED" && ["AGENT","ADMIN"].includes(state.user?.role)
      ? `<button class="table-action" data-action="appointment-status" data-status="CONFIRMED" data-id="${row.id}">Confirm</button><button class="table-action" data-action="appointment-status" data-status="CANCELLED" data-id="${row.id}">Decline</button>`
      : row.status === "CONFIRMED" && state.user?.role === "BUYER" ? `<button class="table-action" data-action="appointment-status" data-status="CANCELLED" data-id="${row.id}">Cancel</button>` : "—" }
  ]);
}

function renderBookings(rows) {
  return table(rows, [
    { label: "Reference", render: row => `<strong>${escape(row.reference)}</strong>` },
    { label: "Property", key: "propertyTitle" },
    { label: "Amount", render: row => money(row.amount) },
    { label: "Status", render: row => `<span class="status-pill ${escape(row.status?.toLowerCase())}">${escape(row.status)}</span>` },
    { label: "Action", render: row => state.user?.role === "BUYER" && row.status === "CONFIRMED"
      ? `<button class="table-action" data-action="demo-payment" data-id="${row.id}">DEMO pay</button>`
      : state.user?.role === "BUYER" && row.status === "REQUESTED" ? `<button class="table-action" data-action="booking-status" data-status="CANCELLED" data-id="${row.id}">Cancel</button>`
      : ["AGENT","ADMIN"].includes(state.user?.role) && row.status === "REQUESTED" ? `<button class="table-action" data-action="booking-status" data-status="CONFIRMED" data-id="${row.id}">Confirm</button><button class="table-action" data-action="booking-status" data-status="CANCELLED" data-id="${row.id}">Decline</button>` : "—" }
  ]);
}

async function renderDashboard() {
  if (!state.user) { authDialog(); return; }
  app.innerHTML = '<div class="dashboard-wrap"><div class="loading-state"><span class="spinner"></span> Loading your workspace…</div></div>';
  try {
    const data = await api("/dashboard");
    let content = "";
    const role = state.user.role;
    if (role === "ADMIN") {
      const users = await api("/admin/users");
      content = `${statsCards(data.stats)}
        <section class="panel"><div class="panel-heading"><div><h2>Property approvals</h2><p>Every listing is reviewed before being shared with the community.</p></div><button class="table-action" data-action="export-properties">Export listings CSV ↗</button></div>${propertyManagementList(data.properties.filter(p => p.status === "PENDING"), true)}</section>
        <section class="panel"><div class="panel-heading"><div><h2>Marketplace listings</h2><p>Current inventory across EstatePro.</p></div></div>${propertyManagementList(data.properties, true)}</section>
        <section class="panel"><div class="panel-heading"><div><h2>People &amp; access</h2><p>Manage account status and access roles.</p></div></div>${table(users, [
          {label:"Name",render:u=>`<strong>${escape(u.fullName)}</strong>`},{label:"Email",key:"email"},{label:"Role",render:u=>`<select class="table-status" data-user-role="${u.id}">${["ADMIN","AGENT","SELLER","BUYER"].map(r=>`<option ${r===u.role?"selected":""}>${r}</option>`).join("")}</select>`},{label:"Access",render:u=>`<select class="table-status" data-user-enabled="${u.id}"><option value="true" ${u.enabled?"selected":""}>Active</option><option value="false" ${!u.enabled?"selected":""}>Disabled</option></select>`},{label:"Action",render:u=>`<button class="table-action" data-action="save-user" data-id="${u.id}">Save access</button>`}
        ])}</section>
        <section class="panel"><div class="panel-heading"><h2>Latest enquiries</h2></div>${renderEnquiries(data.enquiries)}</section>`;
    } else if (role === "AGENT") {
      content = `${statsCards(data.stats)}      <section class="panel"><div class="panel-heading"><div><h2>Your property portfolio</h2><p>Listings assigned to your account.</p></div><button class="button button-dark button-small" data-action="new-property">Add a property <span>↗</span></button></div>${propertyManagementList(data.properties)}</section>
        <section class="panel"><div class="panel-heading"><div><h2>Lead pipeline</h2><p>Keep every conversation moving forward.</p></div></div>${renderEnquiries(data.enquiries)}</section>
        <section class="panel"><div class="panel-heading"><div><h2>Property visits</h2><p>Requests from customers and upcoming viewings.</p></div></div>${renderAppointments(data.appointments)}</section>
        <section class="panel"><div class="panel-heading"><h2>Booking requests</h2></div>${renderBookings(data.bookings)}</section>`;
    } else if (role === "SELLER") {
      content = `${statsCards(data.stats)}<section class="panel"><div class="panel-heading"><div><h2>Your listings</h2><p>New and updated listings go through a review before publishing.</p></div><button class="button button-dark button-small" data-action="new-property">Add a property <span>↗</span></button></div>${propertyManagementList(data.properties)}</section>
        <div id="property-form-slot"></div>`;
    } else {
      let recs = [];
      try { recs = await api("/recommendations"); }
      catch (error) { toast(`Recommendations could not be loaded: ${error.message}`, true); }
      content = `${statsCards(data.stats)}<section class="panel"><div class="panel-heading"><div><h2>Picked for you</h2><p>Homes from the EstatePro collection, ranked for your next chapter.</p></div><button class="table-action" data-action="home">Explore collection ↗</button></div>
        <div class="dashboard-property-list">${recs.map(item=>`<article class="dashboard-property"><img src="${escape(localImage(item.property.imageUrl))}" alt=""><div><strong>${escape(item.property.title)}</strong><small>${escape(item.property.city)} · ${money(item.property.price)}</small><span class="status-pill published">${item.match}% match</span></div><button class="table-action" data-action="details" data-id="${item.property.id}">View</button></article>`).join("") || '<div class="empty-state">Explore listings to find your next home.</div>'}</div></section>
        <section class="panel"><div class="panel-heading"><h2>Your saved homes</h2></div>${propertyManagementList(data.favorites)}</section>
        <section class="panel"><div class="panel-heading"><h2>Your enquiries</h2></div>${table(data.enquiries,[{label:"Property",key:"propertyTitle"},{label:"Message",key:"message"},{label:"Status",render:r=>`<span class="status-pill ${escape(r.status.toLowerCase())}">${escape(r.status)}</span>`},{label:"Date",render:r=>new Date(r.createdAt).toLocaleDateString("en-IN")}])}</section>
        <section class="panel"><div class="panel-heading"><h2>Your visits</h2></div>${renderAppointments(data.appointments)}</section>
        <section class="panel"><div class="panel-heading"><h2>Booking requests</h2></div>${renderBookings(data.bookings)}</section>`;
    }
    app.innerHTML = `<div class="dashboard-wrap"><div class="dashboard-head"><div><p class="eyebrow"><span class="eyebrow-rule"></span> YOUR ESTATEPRO WORKSPACE</p><h1>Good to see you, ${escape(data.name.split(" ")[0])}.</h1><p>${role === "ADMIN" ? "A clear view of your marketplace." : role === "AGENT" ? "Your clients, your listings, your next move." : role === "SELLER" ? "Your property journey, all in one place." : "Your next chapter is taking shape."}</p></div><span class="role-badge">${escape(role.replace("BUYER","BUYER / TENANT"))}</span></div>
      <div class="dashboard-layout"><nav class="dashboard-nav" aria-label="Workspace navigation"><button class="active" data-action="dash-section" data-section="overview">◈ &nbsp; Overview</button><button data-action="dash-section" data-section="properties">⌂ &nbsp; Properties</button><button data-action="dash-section" data-section="enquiries">✳ &nbsp; Enquiries &amp; leads</button><button data-action="dash-section" data-section="appointments">◷ &nbsp; Visits</button><button data-action="dash-section" data-section="bookings">◇ &nbsp; Bookings</button>${role === "BUYER" ? '<button data-action="dash-section" data-section="favorites">♡ &nbsp; Saved homes</button>' : ""}</nav>
      <div class="dashboard-content">${content}</div></div></div>`;
    setHeader();
  } catch (error) {
    app.innerHTML = `<div class="dashboard-wrap"><div class="empty-state">We couldn’t open your workspace. ${escape(error.message)} <button class="table-action" data-action="dashboard">Try again</button></div></div>`;
  }
}

function serializePropertyForm(form) {
  const data = Object.fromEntries(new FormData(form));
  for (const key of ["price", "bedrooms", "bathrooms", "areaSqft"]) data[key] = data[key] === "" ? null : Number(data[key]);
  return data;
}

function showFormError(form, error) {
  const target = form.querySelector(".form-error");
  if (!target) { toast(error.message, true); return; }
  const fieldMessage = error.fields ? Object.entries(error.fields).map(([field, message]) => `${field}: ${message}`).join(" · ") : error.message;
  target.textContent = fieldMessage;
  target.hidden = false;
}

document.addEventListener("submit", async event => {
  const form = event.target;
  if (form.id === "search-form") {
    event.preventDefault();
    const fields = Object.fromEntries(new FormData(form));
    state.filters = Object.fromEntries(Object.entries(fields).filter(([, value]) => value !== ""));
    state.page = 0;
    document.querySelector("#quick-search").value = "";
    await loadProperties();
  } else if (form.id === "auth-form") {
    event.preventDefault();
    const mode = form.dataset.mode;
    const data = Object.fromEntries(new FormData(form));
    try {
      const session = await post(`/auth/${mode}`, data);
      saveSession(session); state.user = session.user; state.favoriteIds = new Set();
      closeDialog(); setHeader(); toast(mode === "register" ? "Welcome to EstatePro. Your account is ready." : "You’re signed in.");
      await loadFavorites();
      await renderDashboard();
    } catch (error) { showFormError(form, error); }
  } else if (form.id === "enquiry-form") {
    event.preventDefault();
    try { await post("/enquiries", { propertyId: form.dataset.property, message: new FormData(form).get("message") }); closeDialog(); toast("Your enquiry has been sent to the property team."); }
    catch (error) { showFormError(form, error); }
  } else if (form.id === "visit-form") {
    event.preventDefault();
    try { await post("/appointments", { propertyId: form.dataset.property, date: form.elements.date.value, time: form.elements.time.value }); closeDialog(); toast("Your visit request is with the property advisor."); }
    catch (error) { showFormError(form, error); }
  } else if (form.id === "property-form") {
    event.preventDefault();
    try {
      const data = serializePropertyForm(form);
      if (form.dataset.id) await put(`/properties/${form.dataset.id}`, data);
      else await post("/properties", data);
      toast(form.dataset.id ? "Your property changes are saved for review." : "Your property has been submitted for review.");
      await renderDashboard();
    } catch (error) { showFormError(form, error); }
  } else if (form.id === "contact-form") {
    event.preventDefault();
    try {
      const request = Object.fromEntries(new FormData(form));
      const response = await post("/contact", request);
      form.reset();
      toast(response.message);
    } catch (error) { showFormError(form, error); }
  }
});

document.addEventListener("click", async event => {
  const button = event.target.closest("[data-action]");
  if (!button) return;
  const action = button.dataset.action;
  if (action !== "browse" && action !== "home" && action !== "menu") event.preventDefault();
  try {
    if (action === "menu") document.querySelector("#main-nav").classList.toggle("open");
    else if (action === "login") authDialog(false);
    else if (action === "register") authDialog(true);
    else if (action === "close-dialog") closeDialog();
    else if (action === "home") { state.filters = {}; state.page = 0; syncHome(); window.scrollTo({ top: 0, behavior: "smooth" }); }
    else if (action === "browse" || action === "city") {
      state.filters = {};
      if (button.dataset.listing) state.filters.listing = button.dataset.listing;
      if (button.dataset.type) state.filters.type = button.dataset.type;
      if (button.dataset.city) state.filters.city = button.dataset.city;
      state.page = 0; syncHome(); document.querySelector("#homes")?.scrollIntoView({ behavior: "smooth" });
    }     else if (action === "about" || action === "agents" || action === "contact")
      document.querySelector(action === "about" ? "#about" : action === "agents" ? "#agents" : "#contact")?.scrollIntoView({ behavior: "smooth" });
    else if (action === "dashboard") await renderDashboard();
    else if (action === "logout") { clearSession(); state.user = null; state.favoriteIds = new Set(); setHeader(); toast("You’ve signed out safely."); syncHome(); }
    else if (action === "details") await openProperty(button.dataset.id);
    else if (action === "favorite") {
      if (!state.user) { authDialog(); toast("Sign in to save your favourite homes."); return; }
      if (state.user.role !== "BUYER") { toast("Saved homes are available to buyer accounts.", true); return; }
      const id = button.dataset.id;
      if (state.favoriteIds?.has(id)) { await remove(`/favorites/${id}`); state.favoriteIds.delete(id); toast("Removed from your saved homes."); }
      else { await post(`/favorites/${id}`, {}); state.favoriteIds.add(id); toast("Saved to your favourites."); }
      await loadProperties();
    } else if (action === "compare") {
      const property = state.properties.find(item => item.id === button.dataset.id);
      if (!property) return;
      state.compare = state.compare.some(item => item.id === property.id) ? state.compare.filter(item => item.id !== property.id) : [...state.compare, property].slice(-3);
      renderCompareDock(); await loadProperties();
    } else if (action === "show-compare") {
      showDialog(`<div class="dialog-shell"><button class="dialog-close" data-action="close-dialog">×</button><p class="eyebrow"><span class="eyebrow-rule"></span> SIDE BY SIDE</p><h2 class="dialog-heading">Compare your shortlist.</h2><p class="dialog-subtitle">A clearer view of the details that matter.</p><div class="comparison-grid">${state.compare.map(p=>`<article class="comparison-cell"><img src="${escape(localImage(p.imageUrl))}" alt=""><strong>${escape(p.title)}</strong><p>${money(p.price)} · ${escape(p.city)}</p><p>${p.bedrooms ?? "—"} beds · ${p.bathrooms ?? "—"} baths</p><p>${Number(p.areaSqft||0).toLocaleString("en-IN")} sq.ft. · ${escape(p.propertyType)}</p></article>`).join("")}</div></div>`);
    } else if (action === "clear-compare") { state.compare = []; renderCompareDock(); await loadProperties(); }
    else if (action === "page") { state.page = Number(button.dataset.page); await loadProperties(); document.querySelector("#homes")?.scrollIntoView({ behavior: "smooth" }); }
    else if (action === "clear-filters") { state.filters = {}; state.page = 0; syncHome(); }
    else if (action === "retry") await loadProperties();
    else if (action === "enquire") enquiryDialog(button.dataset.id);
    else if (action === "visit") visitDialog(button.dataset.id);
    else if (action === "book") {
      if (!state.user) { closeDialog(); authDialog(); toast("Sign in to request a booking."); return; }
      if (state.user.role !== "BUYER") { toast("Booking requests are available to buyer accounts.", true); return; }
      if (!confirm("Send a booking request? No payment will be taken.")) return;
      await post(`/bookings/${button.dataset.id}`, {});
      closeDialog(); toast("Booking request created. No payment has been taken.");
    } else if (action === "new-property") {
      const slot = document.querySelector("#property-form-slot");
      if (slot) { slot.innerHTML = listingForm(); slot.scrollIntoView({ behavior: "smooth" }); }
      else app.querySelector(".dashboard-content")?.insertAdjacentHTML("beforeend", listingForm());
    } else if (action === "edit-property") {
      const property = await api(`/properties/${button.dataset.id}`);
      const slot = app.querySelector(".dashboard-content");
      slot.insertAdjacentHTML("beforeend", listingForm(property));
      slot.lastElementChild.scrollIntoView({ behavior: "smooth" });
    } else if (action === "cancel-edit") button.closest(".dashboard-form").remove();
    else if (action === "archive-property") {
      if (!confirm("Archive this listing? It will no longer appear in your workspace as an active listing.")) return;
      await remove(`/properties/${button.dataset.id}`); toast("Listing archived."); await renderDashboard();
    } else if (action === "moderate") {
      let reason;
      if (button.dataset.status === "REJECTED") {
        reason = prompt("Please provide a reason for rejecting this listing:");
        if (!reason?.trim()) { toast("A reason is required to reject a listing.", true); return; }
      }
      await patch(`/admin/properties/${button.dataset.id}/moderation`, { status: button.dataset.status, reason });
      toast(button.dataset.status === "PUBLISHED" ? "Listing approved and published." : "Listing returned with feedback.");
      await renderDashboard();
    } else if (action === "save-enquiry") {
      const select = app.querySelector(`[data-enquiry="${button.dataset.id}"]`);
      await patch(`/enquiries/${button.dataset.id}`, { status: select.value });
      toast("Lead status updated."); await renderDashboard();
    } else if (action === "appointment-status") {
      await patch(`/appointments/${button.dataset.id}`, { status: button.dataset.status });
      toast("Visit request updated."); await renderDashboard();
    } else if (action === "booking-status") {
      await patch(`/bookings/${button.dataset.id}`, { status: button.dataset.status });
      toast("Booking status updated."); await renderDashboard();
    } else if (action === "demo-payment") {
      if (!confirm("This is a DEMO payment only. No real money will be charged. Continue?")) return;
      const payment = await post(`/payments/${button.dataset.id}/demo`, {});
      toast(`DEMO payment recorded: ${payment.transactionReference}. No funds were transferred.`);
      await renderDashboard();
    } else if (action === "save-user") {
      const role = app.querySelector(`[data-user-role="${button.dataset.id}"]`).value;
      const enabled = app.querySelector(`[data-user-enabled="${button.dataset.id}"]`).value === "true";
      await patch(`/admin/users/${button.dataset.id}`, { role, enabled });
      toast("User access updated."); await renderDashboard();
    } else if (action === "export-properties") {
      const response = await fetch("/api/admin/reports/properties.csv", { headers: { Authorization: `Bearer ${authToken()}` } });
      if (!response.ok) throw new Error("The property report could not be downloaded.");
      const url = URL.createObjectURL(await response.blob());
      const anchor = document.createElement("a"); anchor.href = url; anchor.download = "estatepro-properties.csv"; anchor.click(); URL.revokeObjectURL(url);
    } else if (action === "dash-section") {
      app.querySelectorAll(".dashboard-nav button").forEach(item => item.classList.toggle("active", item === button));
      const section = button.dataset.section;
      const patterns = { properties: /property|listing|saved homes|picked for you/i, enquiries: /enquir|lead pipeline/i, appointments: /visit/i, bookings: /booking/i, favorites: /saved homes/i };
      app.querySelectorAll(".dashboard-content > .panel").forEach(panel => panel.hidden = section !== "overview" && !(patterns[section]?.test(panel.querySelector(".panel-heading")?.textContent || "")));
      const stats = app.querySelector(".dashboard-content > .stats-grid");
      if (stats) stats.hidden = section !== "overview";
    }
  } catch (error) { toast(error.message, true); }
});

document.querySelector("#quick-search").addEventListener("input", event => {
  window.clearTimeout(state.searchTimer);
  state.searchTimer = window.setTimeout(async () => { state.filters.q = event.target.value.trim(); state.page = 0; await loadProperties(); }, 280);
});
document.querySelector("#sort-select").addEventListener("change", async event => { state.sort = event.target.value; state.page = 0; await loadProperties(); });
dialog.addEventListener("click", event => { if (event.target === dialog) closeDialog(); });
document.addEventListener("keydown", event => { if (event.key === "Escape" && dialog.open) closeDialog(); });

async function boot() {
  if (authToken()) {
    try { state.user = await api("/auth/me"); }
    catch { clearSession(); state.user = null; }
  }
  setHeader();
  await loadAgents();
  await loadFavorites();
  await loadProperties();
}
boot();
