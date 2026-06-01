import os, re, glob

specs_dir = ".kiro/specs"
opt_re = re.compile(r'^\s*-\s*\[ \]\s*\*')          # optional pending: - [ ]*
nonopt_re = re.compile(r'^\s*-\s*\[ \](?!\s*\*)')   # non-optional pending: - [ ] (not followed by *)
done_re = re.compile(r'^\s*-\s*\[[xX]\]')

complete_only_optional = []
has_real_pending = []
no_pending = []

for tasks in sorted(glob.glob(os.path.join(specs_dir, "*", "tasks.md"))):
    spec = os.path.basename(os.path.dirname(tasks))
    with open(tasks, encoding="utf-8") as f:
        lines = f.readlines()
    done = sum(1 for l in lines if done_re.match(l))
    opt = sum(1 for l in lines if opt_re.match(l))
    nonopt = sum(1 for l in lines if nonopt_re.match(l))
    if opt == 0 and nonopt == 0:
        no_pending.append((spec, done))
    elif nonopt == 0 and opt > 0:
        complete_only_optional.append((spec, done, opt))
    else:
        has_real_pending.append((spec, done, opt, nonopt))

print("=== COMPLETE (only optional-test pending) — candidates to CLOSE ===")
for s, d, o in complete_only_optional:
    print(f"  {s}: done={d}, optional_pending={o}")
print(f"  TOTAL: {len(complete_only_optional)} specs")

print("\n=== HAS REAL (non-test) PENDING — keep ===")
for s, d, o, n in has_real_pending:
    print(f"  {s}: done={d}, optional={o}, NON-OPTIONAL={n}")
print(f"  TOTAL: {len(has_real_pending)} specs")

print("\n=== NO PENDING (already clean) ===")
for s, d in no_pending:
    print(f"  {s}: done={d}")
print(f"  TOTAL: {len(no_pending)} specs")
