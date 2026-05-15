#!/usr/bin/env python3
"""
Test 2 — Boilerplate Counter

Counts infrastructure lines in microservices implementations vs monolith equivalents.
Quantifies the "Reasoning Tax" — extra code agents write in microservices that has
nothing to do with the actual business problem.

Usage:
    python count_boilerplate.py --monolith library/monolith --microservices library/microservices
    python count_boilerplate.py --all        # all 4 domains
    python count_boilerplate.py --file path/to/SomeService.java

Boilerplate categories tracked:
    HTTP_CLIENT     RestTemplate, WebClient, HttpClient, Feign declarations
    HTTP_RESPONSE   ResponseEntity, getBody(), getStatusCode(), exchange()
    DTO             Classes that only hold fields (data transfer objects)
    ERROR_HANDLER   catch(HttpClientErrorException), try/catch around HTTP calls
    JSON_MAPPING    ObjectMapper, JSON parsing, @JsonProperty
    SERVICE_DISCO   @FeignClient, @LoadBalanced, @DiscoveryClient, EurekaClient
"""

import argparse
import os
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List


# ─── Pattern definitions ──────────────────────────────────────────────────────

BOILERPLATE_PATTERNS = {
    "HTTP_CLIENT": [
        r"RestTemplate",
        r"WebClient",
        r"HttpClient",
        r"@FeignClient",
        r"FeignClient",
        r"OkHttpClient",
        r"CloseableHttpClient",
    ],
    "HTTP_RESPONSE": [
        r"ResponseEntity",
        r"\.getBody\(\)",
        r"\.getStatusCode\(\)",
        r"\.exchange\(",
        r"\.getForObject\(",
        r"\.postForObject\(",
        r"\.getForEntity\(",
        r"HttpEntity",
        r"HttpHeaders",
    ],
    "ERROR_HANDLER": [
        r"HttpClientErrorException",
        r"HttpServerErrorException",
        r"RestClientException",
        r"catch\s*\(\s*Exception",
        r"catch\s*\(\s*IOException",
        r"catch\s*\(\s*RuntimeException",
        r"@Retryable",
        r"@CircuitBreaker",
        r"fallback",
    ],
    "JSON_MAPPING": [
        r"ObjectMapper",
        r"@JsonProperty",
        r"readValue\(",
        r"writeValueAsString\(",
        r"Map\.of\(",
        r"HashMap<String",
        r"new TypeReference",
    ],
    "SERVICE_DISCO": [
        r"@LoadBalanced",
        r"@DiscoveryClient",
        r"EurekaClient",
        r"DiscoveryClient",
        r"ServiceInstance",
    ],
    "DTO": [
        r"Dto\b",
        r"DTO\b",
        r"Response\b",
        r"Request\b",
        r"@JsonDeserialize",
        r"@JsonSerialize",
    ],
}

# Lines that are pure business logic in the monolith
MONOLITH_DIRECT_CALL_PATTERNS = [
    r"@Autowired",
    r"@RequiredArgsConstructor",
    r"private final \w+Service",
    r"\w+Service\.\w+\(",  # direct service method call
]


@dataclass
class FileAnalysis:
    path: str
    total_lines: int
    code_lines: int  # non-blank, non-comment
    boilerplate_lines: int
    boilerplate_by_category: Dict[str, int] = field(default_factory=dict)
    boilerplate_ratio: float = 0.0


@dataclass
class DirectoryAnalysis:
    path: str
    files: List[FileAnalysis] = field(default_factory=list)

    @property
    def total_code_lines(self): return sum(f.code_lines for f in self.files)
    @property
    def total_boilerplate(self): return sum(f.boilerplate_lines for f in self.files)
    @property
    def overall_boilerplate_ratio(self):
        return self.total_boilerplate / self.total_code_lines if self.total_code_lines else 0
    @property
    def by_category(self):
        result = {}
        for f in self.files:
            for cat, count in f.boilerplate_by_category.items():
                result[cat] = result.get(cat, 0) + count
        return result


def is_code_line(line: str) -> bool:
    stripped = line.strip()
    if not stripped:
        return False
    if stripped.startswith("//") or stripped.startswith("*") or stripped.startswith("/*"):
        return False
    if stripped in ("{", "}", "@Override"):
        return False
    return True


def analyse_file(path: Path) -> FileAnalysis:
    try:
        lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    except Exception:
        return FileAnalysis(str(path), 0, 0, 0)

    total = len(lines)
    code_lines = [l for l in lines if is_code_line(l)]
    code_count = len(code_lines)

    boilerplate_by_cat = {cat: 0 for cat in BOILERPLATE_PATTERNS}
    boilerplate_line_set = set()

    for i, line in enumerate(code_lines):
        for cat, patterns in BOILERPLATE_PATTERNS.items():
            for pat in patterns:
                if re.search(pat, line):
                    boilerplate_line_set.add(i)
                    boilerplate_by_cat[cat] += 1
                    break

    bp_count = len(boilerplate_line_set)
    return FileAnalysis(
        path=str(path),
        total_lines=total,
        code_lines=code_count,
        boilerplate_lines=bp_count,
        boilerplate_by_category=boilerplate_by_cat,
        boilerplate_ratio=bp_count / code_count if code_count else 0.0,
    )


def analyse_directory(base_path: str) -> DirectoryAnalysis:
    base = Path(base_path)
    result = DirectoryAnalysis(base_path)
    for java_file in base.rglob("*.java"):
        result.files.append(analyse_file(java_file))
    return result


def print_analysis(label: str, analysis: DirectoryAnalysis):
    print(f"\n{'─'*60}")
    print(f"{label}: {analysis.path}")
    print(f"{'─'*60}")
    print(f"  Java files analysed : {len(analysis.files)}")
    print(f"  Total code lines    : {analysis.total_code_lines}")
    print(f"  Boilerplate lines   : {analysis.total_boilerplate}")
    print(f"  Boilerplate ratio   : {analysis.overall_boilerplate_ratio:.1%}")
    print(f"\n  By category:")
    for cat, count in sorted(analysis.by_category.items(), key=lambda x: -x[1]):
        if count > 0:
            print(f"    {cat:<20} {count} lines")


def compare(monolith: DirectoryAnalysis, microservices: DirectoryAnalysis):
    print(f"\n{'='*60}")
    print(f"COMPARISON: Monolith vs Microservices")
    print(f"{'='*60}")

    m_code   = monolith.total_code_lines
    ms_code  = microservices.total_code_lines
    m_bp     = monolith.total_boilerplate
    ms_bp    = microservices.total_boilerplate
    extra_bp = ms_bp - m_bp
    extra_lines = ms_code - m_code

    print(f"  Monolith code lines      : {m_code}")
    print(f"  Microservices code lines : {ms_code}")
    print(f"  Extra lines in micros    : +{extra_lines}")
    print(f"")
    print(f"  Monolith boilerplate     : {m_bp} ({monolith.overall_boilerplate_ratio:.1%})")
    print(f"  Microservices boilerplate: {ms_bp} ({microservices.overall_boilerplate_ratio:.1%})")
    print(f"  Extra boilerplate        : +{extra_bp} lines")
    if extra_lines > 0:
        bp_pct = extra_bp / extra_lines * 100
        print(f"  Of extra lines, {bp_pct:.0f}% is infrastructure (not business logic)")
    print(f"")
    print(f"  REASONING TAX: An agent implementing a feature in microservices")
    print(f"  writes {microservices.overall_boilerplate_ratio:.0%} infrastructure vs {monolith.overall_boilerplate_ratio:.0%} in the monolith.")
    ratio = microservices.overall_boilerplate_ratio / monolith.overall_boilerplate_ratio if monolith.overall_boilerplate_ratio else float('inf')
    print(f"  Infrastructure overhead is {ratio:.1f}x higher in microservices.")


DOMAIN_PATHS = {
    "library":       ("library/monolith",      "library/microservices"),
    "healthcare":    ("healthcare/monolith",    "healthcare/microservices"),
    "insurance":     ("insurance/monolith",     "insurance/microservices"),
    "supply-chain":  ("supply-chain/monolith",  "supply-chain/microservices"),
}


def main():
    parser = argparse.ArgumentParser(description="ModulithBench Test 2 — Boilerplate Counter")
    parser.add_argument("--monolith", help="Path to monolith src directory")
    parser.add_argument("--microservices", help="Path to microservices src directory")
    parser.add_argument("--domain", choices=list(DOMAIN_PATHS.keys()), help="Analyse a whole domain")
    parser.add_argument("--all", action="store_true", help="Analyse all domains")
    parser.add_argument("--file", help="Analyse a single Java file")
    args = parser.parse_args()

    if args.file:
        fa = analyse_file(Path(args.file))
        print(f"\nFile: {fa.path}")
        print(f"  Code lines      : {fa.code_lines}")
        print(f"  Boilerplate     : {fa.boilerplate_lines} ({fa.boilerplate_ratio:.1%})")
        for cat, count in fa.boilerplate_by_category.items():
            if count > 0:
                print(f"    {cat}: {count}")
        return

    domains = list(DOMAIN_PATHS.keys()) if args.all else ([args.domain] if args.domain else [])

    if not domains and args.monolith and args.microservices:
        m = analyse_directory(args.monolith)
        ms = analyse_directory(args.microservices)
        print_analysis("Monolith", m)
        print_analysis("Microservices", ms)
        compare(m, ms)
        return

    if not domains:
        parser.print_help()
        return

    for domain in domains:
        m_path, ms_path = DOMAIN_PATHS[domain]
        if not os.path.exists(m_path) or not os.path.exists(ms_path):
            print(f"Skipping {domain} — paths not found ({m_path}, {ms_path})")
            continue
        m = analyse_directory(m_path)
        ms = analyse_directory(ms_path)
        print(f"\n{'#'*60}")
        print(f"# DOMAIN: {domain.upper()}")
        print(f"{'#'*60}")
        print_analysis("Monolith", m)
        print_analysis("Microservices", ms)
        compare(m, ms)


if __name__ == "__main__":
    main()
