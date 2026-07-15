# Business Model: Fibre-Crop-Growing Operations Coordinator

## Classification

- Repository: `cloud-itonami-isic-0116`
- ISIC Rev. 4: `0116`
- Industry: Growing of fibre crops
- Social impact: food-security, rural-employment, environmental-stewardship

## Customer

- Small-to-medium fibre-crop farms (cotton, jute, flax, hemp, sisal)
- Textile-fibre cooperatives and contract growers
- Diversified row-crop operations that include fibre-crop acreage
- Smallholder fibre producers (extension-service integrations)

## Offer

- Field management and record-keeping (planting/fibre-yield/quality-grade)
- Planting/defoliation/retting/harvest scheduling coordination
- Crop-health and pest/disease tracking (e.g. boll weevil)
- Supply procurement coordination
- Audit trail and transparency

## Revenue

- SaaS subscription (per-hectare-per-season pricing)
- Supply chain integration fees
- API access for agronomist/extension-service partners
- Data analytics and reporting add-ons

## Trust Controls

- No direct field-equipment operation without human sign-off
- No finalized pesticide-application decisions by the actor
- All field-operation scheduling proposals are proposals, not commands
- Field registration is required before any operation
- All crop-health concerns are automatically escalated
- High-cost supply orders require approval
- Logged fibre-quality grades are independently verified against a closed
  vocabulary before a record proposal is considered
- Audit ledger is append-only and never editable

## What we do NOT do

- **Agronomic decisions** (what/when/how much to plant, defoliate, ret, or
  harvest) — the farmer/agronomist decides
- **Pesticide-application decisions** — the agronomist/farmer decides
- **Direct field-equipment operation** — the robot manages records and logistics only
- **Economic decisions** (crop mix, marketing, land use) — remain human authority

## Supported Operations

### Field Record Logging
- Planting records (crop, acreage, date)
- Fibre-yield records
- Quality-grade records (closed vocabulary — see `fibreops.facts`)
- Field-condition notes (logging only, not decision-making)

### Field-Operation Scheduling
- Schedule planting, defoliation, retting, harvest windows
- Track equipment/labor availability
- Propose follow-up field visits (not order them directly)

### Crop-Health Concern Escalation
- Flag suspected pest infestation (e.g. boll weevil)
- Report disease symptoms or drought stress
- Automatic escalation to farmer/agronomist

### Supply Procurement
- Seed orders
- Fertilizer orders
- Equipment procurement
- Cost threshold escalation for large orders
