import os
import sys
from reportlab.lib.pagesizes import letter, A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_decorations(self, page_count):
        if self._pageNumber == 1:
            return  # Skip header and footer on cover page
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748b"))

        # Header
        self.drawString(54, 800, "CampusFace - Real-Time Face Recognition Attendance System")
        self.drawRightString(540, 800, "Technical & Architecture Specification")
        self.setStrokeColor(colors.HexColor("#cbd5e1"))
        self.setLineWidth(0.5)
        self.line(54, 792, 540, 792)

        # Footer
        self.line(54, 45, 540, 45)
        self.drawString(54, 34, "B.Tech Computer Science & Engineering Capstone Project")
        self.drawRightString(540, 34, f"Page {self._pageNumber} of {page_count}")
        self.restoreState()

def build_pdf(filename="CampusFace_Project_Documentation.pdf"):
    doc = SimpleDocTemplate(
        filename,
        pagesize=A4,
        leftMargin=54,
        rightMargin=54,
        topMargin=54,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()

    # Custom Palettes
    c_primary = colors.HexColor("#1e3a8a")     # Deep Navy
    c_dark = colors.HexColor("#0f172a")        # Dark Slate
    c_accent = colors.HexColor("#2563eb")      # Royal Blue
    c_success = colors.HexColor("#10b981")     # Emerald Green
    c_muted = colors.HexColor("#475569")       # Muted Slate
    c_card_bg = colors.HexColor("#f8fafc")     # Light Slate Background
    c_border = colors.HexColor("#e2e8f0")

    # Typography Styles
    title_style = ParagraphStyle(
        'CoverTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=28,
        leading=34,
        textColor=c_dark,
        alignment=1,
        spaceAfter=15
    )

    subtitle_style = ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=13,
        leading=18,
        textColor=c_muted,
        alignment=1,
        spaceAfter=30
    )

    institution_style = ParagraphStyle(
        'InstitutionHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=16,
        textColor=c_primary,
        alignment=1,
        spaceAfter=6
    )

    badge_style = ParagraphStyle(
        'Badge',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=12,
        textColor=c_accent,
        alignment=1,
        spaceAfter=15
    )

    h1_style = ParagraphStyle(
        'SectionH1',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=18,
        leading=22,
        textColor=c_primary,
        spaceBefore=20,
        spaceAfter=10,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'SectionH2',
        parent=styles['Heading2'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=17,
        textColor=c_dark,
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyDark',
        parent=styles['BodyText'],
        fontName='Helvetica',
        fontSize=10,
        leading=15,
        textColor=c_dark,
        alignment=4,  # Justified
        spaceAfter=8
    )

    bullet_style = ParagraphStyle(
        'BulletItem',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=14,
        textColor=c_dark,
        leftIndent=15,
        spaceAfter=4
    )

    code_style = ParagraphStyle(
        'CodeSnippet',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8.5,
        leading=12,
        textColor=colors.HexColor("#0f172a"),
        spaceAfter=4
    )

    table_header_style = ParagraphStyle(
        'TableHeader',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9,
        leading=12,
        textColor=c_dark
    )

    table_cell_style = ParagraphStyle(
        'TableCell',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.5,
        leading=11.5,
        textColor=c_dark
    )

    table_cell_bold = ParagraphStyle(
        'TableCellBold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=11.5,
        textColor=c_primary
    )

    callout_style = ParagraphStyle(
        'CalloutText',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9.5,
        leading=13.5,
        textColor=colors.HexColor("#1e40af")
    )

    equation_style = ParagraphStyle(
        'EquationText',
        parent=styles['Normal'],
        fontName='Times-BoldItalic',
        fontSize=11,
        leading=16,
        alignment=1,
        textColor=c_dark
    )

    story = []

    # ================= COVER PAGE =================
    story.append(Spacer(1, 40))
    story.append(Paragraph("DEPARTMENT OF COMPUTER SCIENCE &amp; ENGINEERING", institution_style))
    story.append(Paragraph("B.Tech Capstone Project &bull; Technical Specification Manual", ParagraphStyle('SubSub', parent=subtitle_style, fontSize=10, textColor=c_muted, spaceAfter=40)))

    story.append(Paragraph("[ BIOMETRIC DESKTOP SOFTWARE ]", badge_style))
    story.append(Paragraph("CampusFace", title_style))
    story.append(Paragraph("Automated Real-Time Face Recognition Attendance Management System with Dual-Engine Architecture &amp; Continuous Biometric Verification", subtitle_style))

    story.append(Spacer(1, 20))

    meta_data = [
        [Paragraph("<b>Project Title:</b>", table_header_style), Paragraph("CampusFace Desktop Attendance System", table_cell_style)],
        [Paragraph("<b>Domain &amp; Field:</b>", table_header_style), Paragraph("Computer Vision, Biometrics &amp; Relational Databases", table_cell_style)],
        [Paragraph("<b>Technology Stack:</b>", table_header_style), Paragraph("Java (JDK 17/21), Java Swing, FlatLaf 3.4.1", table_cell_style)],
        [Paragraph("<b>Biometric Algorithm:</b>", table_header_style), Paragraph("1024-D Local Binary Pattern Histograms (LBPH) + &chi;<sup>2</sup> Distance", table_cell_style)],
        [Paragraph("<b>Database Engine:</b>", table_header_style), Paragraph("Embedded SQLite 3 (Default) / MySQL 8.0 (Dual Support)", table_cell_style)],
        [Paragraph("<b>Camera Interface:</b>", table_header_style), Paragraph("Native DirectShow / V4L2 USB Hardware Webcams &amp; Virtual Feeds", table_cell_style)],
        [Paragraph("<b>Academic Context:</b>", table_header_style), Paragraph("B.Tech Computer Science Final Year Project Submission", table_cell_style)],
        [Paragraph("<b>Release Version:</b>", table_header_style), Paragraph("Version 2.4 (Production Release)", table_cell_style)]
    ]

    t_meta = Table(meta_data, colWidths=[140, 320])
    t_meta.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_card_bg),
        ('BOX', (0,0), (-1,-1), 1, c_border),
        ('INNERGRID', (0,0), (-1,-1), 0.5, c_border),
        ('TOPPADDING', (0,0), (-1,-1), 7),
        ('BOTTOMPADDING', (0,0), (-1,-1), 7),
        ('LEFTPADDING', (0,0), (-1,-1), 12),
        ('RIGHTPADDING', (0,0), (-1,-1), 12),
    ]))
    story.append(t_meta)

    story.append(Spacer(1, 40))
    story.append(Paragraph("<i>A comprehensive document covering system architecture, computer vision algorithms, tools utilized, exception handling models, relational database schemas, and academic viva questions.</i>", ParagraphStyle('CoverNote', parent=subtitle_style, fontSize=9, alignment=1)))
    story.append(PageBreak())

    # ================= SECTION 1: EXECUTIVE SUMMARY =================
    story.append(Paragraph("1. Executive Summary &amp; Problem Definition", h1_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_primary, spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("<b>1.1 Project Overview</b>", h2_style))
    story.append(Paragraph(
        "<b>CampusFace</b> is an automated biometric desktop attendance management system engineered in pure Java. "
        "It eliminates outdated, paper-based roll-call procedures and prevents proxy attendance in college classrooms and laboratories. "
        "The system captures real-time video frames from USB hardware webcams, locates the facial region using skin-tone chromaticity segmentation, "
        "extracts normalized 1024-dimensional Local Binary Pattern Histogram (LBPH) feature vectors, and verifies student identities against "
        "enrolled database records in under 80 milliseconds.",
        body_style
    ))
    story.append(Paragraph(
        "Once verified, the on-screen camera display immediately overlays glowing emerald alignment brackets with a prominent <b>'&#10004; PRESENT'</b> badge, "
        "sounds an audible confirmation chime, records attendance for the current lecture session in the database, and dynamically adds the student to "
        "the live attendee feed table.",
        body_style
    ))

    story.append(Paragraph("<b>1.2 Problems with Traditional Attendance Systems</b>", h2_style))
    story.append(Paragraph("&bull; <b>Significant Lecture Disruption:</b> Calling out roll numbers for 60 to 80 students consumes 10 to 15 minutes of every lecture, wasting over 15% of instructional time.", bullet_style))
    story.append(Paragraph("&bull; <b>Prevalence of Proxy Attendance:</b> Paper sign-in sheets make it trivial for students to forge signatures on behalf of absent peers.", bullet_style))
    story.append(Paragraph("&bull; <b>Administrative Burden &amp; Compilation Errors:</b> Calculating cumulative monthly attendance across courses and divisions requires labor-intensive compilation from paper registers, leading to human error and student disputes.", bullet_style))
    story.append(Paragraph("&bull; <b>Lack of Real-Time Analytics:</b> Neither faculty nor academic administrators possess immediate visibility into class-wide or student-specific attendance percentages.", bullet_style))

    story.append(Paragraph("<b>1.3 Key Project Objectives</b>", h2_style))
    story.append(Paragraph("&bull; <b>Instant Frictionless Access:</b> Provide direct 1-click launching into the face attendance scanner without requiring passwords just to scan students.", bullet_style))
    story.append(Paragraph("&bull; <b>Continuous Scanning Mode:</b> Maintain an uninterrupted video stream where an entire class of students can be scanned consecutively without modal popups closing the window.", bullet_style))
    story.append(Paragraph("&bull; <b>5-Second Camera Enrollment:</b> Allow new students to be registered and their biometric facial templates enrolled directly from the live camera stream in under 5 seconds.", bullet_style))
    story.append(Paragraph("&bull; <b>Zero-Setup Embedded Database:</b> Operate seamlessly with zero database installation by automatically embedding and seeding a portable SQLite 3 database.", bullet_style))

    # ================= SECTION 2: SYSTEM ARCHITECTURE & WORKFLOW =================
    story.append(Spacer(1, 10))
    story.append(Paragraph("2. System Architecture &amp; Operational Pipeline", h1_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_primary, spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("The system is divided into six progressive stages forming a real-time computer vision and data persistence pipeline:", body_style))

    pipeline_data = [
        [Paragraph("<b>Stage</b>", table_header_style), Paragraph("<b>Component / Class</b>", table_header_style), Paragraph("<b>Technical Operation</b>", table_header_style)],
        [Paragraph("1. Acquisition", table_cell_bold), Paragraph("<font face='Courier'>CameraManager</font>", table_cell_style), Paragraph("Background daemon thread captures 640x480 RGB frames at ~25 FPS via native DirectShow / BridJ drivers.", table_cell_style)],
        [Paragraph("2. Detection", table_cell_bold), Paragraph("<font face='Courier'>FaceRecognitionEngine</font>", table_cell_style), Paragraph("Evaluates skin-tone chrominance (YCbCr) to detect facial boundaries with proportional center fallback.", table_cell_style)],
        [Paragraph("3. Normalization", table_cell_bold), Paragraph("<font face='Courier'>FaceRecognitionEngine</font>", table_cell_style), Paragraph("Crops face, resizes to standardized 100x100 pixels, converts to grayscale, and applies Histogram Equalization.", table_cell_style)],
        [Paragraph("4. Feature Extraction", table_cell_bold), Paragraph("<font face='Courier'>FaceRecognitionEngine</font>", table_cell_style), Paragraph("Divides 100x100 face into 8x8 cells, quantizes 256 LBP codes into 16 bins per cell, producing a 1024-D vector.", table_cell_style)],
        [Paragraph("5. Distance Matching", table_cell_bold), Paragraph("<font face='Courier'>FaceRecognitionService</font>", table_cell_style), Paragraph("Nearest-Neighbor matching against in-memory enrolled templates using Chi-Square (&chi;<sup>2</sup>) distance.", table_cell_style)],
        [Paragraph("6. Persistence &amp; HUD", table_cell_bold), Paragraph("<font face='Courier'>AttendanceService</font> &amp; <font face='Courier'>RecognitionFrame</font>", table_cell_style), Paragraph("Records attendance in SQLite, updates on-screen emerald HUD badge, plays audio chime, and updates live feed table.", table_cell_style)]
    ]

    t_pipe = Table(pipeline_data, colWidths=[90, 130, 260])
    t_pipe.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#e2e8f0")),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_pipe)

    story.append(PageBreak())

    # ================= SECTION 3: TOOLS & TECHNOLOGIES =================
    story.append(Paragraph("3. Tools, Technologies &amp; Libraries Utilized", h1_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_primary, spaceBefore=2, spaceAfter=12))

    story.append(Paragraph(
        "CampusFace is built using a curated suite of robust, industry-standard technologies to ensure cross-platform compatibility, zero external runtime prerequisites, and high computational efficiency:",
        body_style
    ))

    tools_data = [
        [Paragraph("<b>Category</b>", table_header_style), Paragraph("<b>Technology / Tool</b>", table_header_style), Paragraph("<b>Version</b>", table_header_style), Paragraph("<b>Role and Functionality in Project</b>", table_header_style)],
        [Paragraph("Language Core", table_cell_bold), Paragraph("Java (JDK)", table_cell_style), Paragraph("17 / 21", table_cell_style), Paragraph("Primary object-oriented runtime platform providing multithreading, concurrency, 2D graphics, and JDBC standard libraries.", table_cell_style)],
        [Paragraph("GUI Framework", table_cell_bold), Paragraph("Java Swing", table_cell_style), Paragraph("JDK Core", table_cell_style), Paragraph("Desktop Graphical User Interface framework managing windows, layout managers, custom drawing surfaces, and event dispatching.", table_cell_style)],
        [Paragraph("UI Styling", table_cell_bold), Paragraph("FlatLaf", table_cell_style), Paragraph("3.4.1", table_cell_style), Paragraph("Modern Flat Light Look and Feel delivering rounded borders, card shadows, high-DPI font rendering, and clean visual themes.", table_cell_style)],
        [Paragraph("Webcam Driver", table_cell_bold), Paragraph("webcam-capture", table_cell_style), Paragraph("0.3.12", table_cell_style), Paragraph("Hardware camera abstraction layer interfacing Java applications directly with OS webcam drivers (DirectShow on Windows, V4L2 on Linux).", table_cell_style)],
        [Paragraph("Native C Bridge", table_cell_bold), Paragraph("BridJ", table_cell_style), Paragraph("0.7.0", table_cell_style), Paragraph("High-performance native memory bridge linking C/C++ native dynamic link libraries (.dll) with the Java Virtual Machine without JNI boilerplate.", table_cell_style)],
        [Paragraph("Embedded DB", table_cell_bold), Paragraph("SQLite JDBC", table_cell_style), Paragraph("3.45.1.0", table_cell_style), Paragraph("Zero-configuration embedded database engine storing student records, attendance events, and biometric encodings in <font face='Courier'>attendance_system.db</font>.", table_cell_style)],
        [Paragraph("Enterprise DB", table_cell_bold), Paragraph("MySQL Connector/J", table_cell_style), Paragraph("8.3.0", table_cell_style), Paragraph("Optional enterprise relational connector allowing dual-engine switching via simple property changes in <font face='Courier'>db.properties</font>.", table_cell_style)],
        [Paragraph("Cryptography", table_cell_bold), Paragraph("SHA-256 Digest", table_cell_style), Paragraph("JDK Core", table_cell_style), Paragraph("One-way cryptographic hashing securing administrative passwords against reverse-engineering and dictionary attacks.", table_cell_style)],
        [Paragraph("Logging", table_cell_bold), Paragraph("SLF4J Simple", table_cell_style), Paragraph("1.7.2", table_cell_style), Paragraph("Lightweight logging framework tracking hardware webcam initialization, frame rates, and database transaction lifecycles.", table_cell_style)]
    ]

    t_tools = Table(tools_data, colWidths=[80, 100, 50, 250])
    t_tools.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#e2e8f0")),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_tools)

    # ================= SECTION 4: CONCEPTS & MATHEMATICS =================
    story.append(Spacer(1, 10))
    story.append(Paragraph("4. Core Technical Concepts &amp; Mathematical Models", h1_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_primary, spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("<b>4.1 Model-View-Controller (MVC) Pattern</b>", h2_style))
    story.append(Paragraph(
        "CampusFace strictly adheres to the MVC architectural paradigm to separate concerns between user interface, business logic, and relational persistence:",
        body_style
    ))
    story.append(Paragraph("&bull; <b>Model:</b> Pure domain entities holding data state (<font face='Courier'>User</font>, <font face='Courier'>Student</font>, <font face='Courier'>FaceData</font>, <font face='Courier'>Attendance</font>).", bullet_style))
    story.append(Paragraph("&bull; <b>Data Access Object (DAO):</b> Encapsulates all SQL CRUD queries via parameterized <font face='Courier'>PreparedStatement</font> objects to prevent SQL injection vulnerabilities.", bullet_style))
    story.append(Paragraph("&bull; <b>Service Layer:</b> Implements core business logic including nearest-neighbor biometric matching, duplicate attendance checks, and in-memory template caching.", bullet_style))
    story.append(Paragraph("&bull; <b>View:</b> Swing GUI windows managing user interaction, video stream rendering, FlatLaf styling, and feedback dialogs.", bullet_style))

    story.append(Paragraph("<b>4.2 Skin Chromaticity Segmentation (YCbCr Space)</b>", h2_style))
    story.append(Paragraph(
        "RGB color values vary drastically based on ambient room illumination. To locate human faces efficiently without GPU neural networks, "
        "incoming RGB frames are converted into the YCbCr color space using the standard ITU-R BT.601 transformation matrix:",
        body_style
    ))

    story.append(Paragraph("<i>Y = 0.299R + 0.587G + 0.114B &nbsp;|&nbsp; C<sub>b</sub> = 128 - 0.1687R - 0.3313G + 0.5B &nbsp;|&nbsp; C<sub>r</sub> = 128 + 0.5R - 0.4187G - 0.0813B</i>", equation_style))

    story.append(Paragraph(
        "Skin chromaticity boundaries in human skin cluster tightly within the chrominance plane regardless of lighting or ethnicity. A pixel is marked as human skin if:",
        body_style
    ))
    story.append(Paragraph("<b>75 &le; C<sub>b</sub> &le; 138 &nbsp;&nbsp;AND&nbsp;&nbsp; 128 &le; C<sub>r</sub> &le; 178 &nbsp;&nbsp;AND&nbsp;&nbsp; Y &ge; 40</b>", equation_style))

    story.append(PageBreak())

    story.append(Paragraph("<b>4.3 Grayscale Contrast Histogram Equalization</b>", h2_style))
    story.append(Paragraph(
        "To ensure that facial recognition performs accurately in classrooms with shadows or backlighting, the detected face crop is converted to grayscale "
        "and normalized via Histogram Equalization. This spreads the most frequent intensity values across the full 256 grayscale range by linearizing the Cumulative Distribution Function (CDF):",
        body_style
    ))
    story.append(Paragraph("<b>h(v) = round( [ (CDF(v) - CDF<sub>min</sub>) / ( (W &times; H) - CDF<sub>min</sub> ) ] &times; 255 )</b>", equation_style))

    story.append(Paragraph("<b>4.4 Local Binary Patterns Histogram (LBPH) Algorithm</b>", h2_style))
    story.append(Paragraph(
        "LBPH is an established texture description algorithm that captures micro-patterns in facial features (eyebrows, nose bridges, jawline contours). "
        "The mathematical execution proceeds in four distinct steps:",
        body_style
    ))
    story.append(Paragraph("1. <b>8-Neighbor Thresholding:</b> For every pixel <i>p<sub>c</sub></i>, each of its 8 surrounding neighbors <i>p<sub>n</sub></i> (n = 0...7) is compared against <i>p<sub>c</sub></i>. If neighbor intensity &ge; center intensity, bit is 1; otherwise 0.", bullet_style))
    story.append(Paragraph("2. <b>Binary to Decimal Encoding:</b> The 8 binary bits are weighted by powers of 2 to yield a decimal code between 0 and 255:", bullet_style))
    story.append(Paragraph("<b>LBP(x<sub>c</sub>, y<sub>c</sub>) = &sum;<sub>n=0</sub><sup>7</sup> s(i<sub>n</sub> - i<sub>c</sub>) &times; 2<sup>n</sup></b>", equation_style))
    story.append(Paragraph("3. <b>Spatial Grid Cell Division:</b> The normalized 100x100 face is segmented into an <b>8 &times; 8 grid of 64 cells</b> (each 12x12 pixels).", bullet_style))
    story.append(Paragraph("4. <b>Local Histogram Quantization &amp; Concatenation:</b> A 16-bin histogram is computed for each cell by quantizing the 256 LBP codes. Each cell histogram is L1-normalized (&sum;h = 1.0) and concatenated into a single master feature vector:", bullet_style))
    story.append(Paragraph("<b>Vector Length = Grid<sub>X</sub> (8) &times; Grid<sub>Y</sub> (8) &times; Bins (16) = 1024 Features</b>", equation_style))

    story.append(Paragraph("<b>4.5 Chi-Square (&chi;<sup>2</sup>) Distance Metric &amp; Confidence Formulation</b>", h2_style))
    story.append(Paragraph(
        "When matching a live camera vector <b>u</b> against an enrolled template vector <b>v</b>, the Chi-Square dissimilarity distance is computed:",
        body_style
    ))
    story.append(Paragraph("<b>D(u, v) = 0.5 &times; &sum;<sub>i=1</sub><sup>1024</sup> [ (u<sub>i</sub> - v<sub>i</sub>)<sup>2</sup> / (u<sub>i</sub> + v<sub>i</sub> + &epsilon;) ]</b>", equation_style))
    story.append(Paragraph(
        "A lower distance denotes closer identity resemblance. If distance D &le; 11.0, the match is confirmed. The distance is converted to an intuitive Confidence Score (0% to 100%):",
        body_style
    ))
    story.append(Paragraph("<b>Confidence % = 60.0% + [ (1.0 - D / 11.0) &times; 39.4% ]</b> &nbsp;&nbsp;<i>(if D &le; 11.0)</i>", equation_style))

    story.append(Paragraph("<b>4.6 Relational Integrity &amp; Composite Unique Constraints</b>", h2_style))
    story.append(Paragraph(
        "Duplicate attendance is strictly blocked both at the application logic layer and at the database engine level via a composite unique constraint in the attendance table: "
        "<font face='Courier'>UNIQUE(student_id, attendance_date, session_id)</font>. This guarantees that even if a student stands in front of the camera for minutes, they are recorded exactly once per lecture session.",
        body_style
    ))

    story.append(PageBreak())

    # ================= SECTION 5: EXCEPTIONS & ERROR HANDLING =================
    story.append(Paragraph("5. Comprehensive Exceptions &amp; Error Handling", h1_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_primary, spaceBefore=2, spaceAfter=12))

    story.append(Paragraph(
        "Desktop software interacting with hardware drivers, multithreaded loops, and relational files must handle failure conditions gracefully. "
        "The following table comprehensively details the exceptions encountered in the system, their root causes, and their exact programmatic mitigations:",
        body_style
    ))

    exc_data = [
        [Paragraph("<b>Exception Class</b>", table_header_style), Paragraph("<b>Root Cause / Scenario</b>", table_header_style), Paragraph("<b>Programmatic Mitigation &amp; Recovery Strategy</b>", table_header_style)],
        [
            Paragraph("<font face='Courier'>WebcamException</font> / <font face='Courier'>WebcamLockException</font>", table_cell_bold),
            Paragraph("USB webcam is unplugged, unpowered, or locked by another application (e.g., Zoom, Teams, Google Meet).", table_cell_style),
            Paragraph("Intercepted cleanly during startup. The application automatically falls back to Virtual Camera Mode, notifies the user via an amber status badge, and provides a 1-click '📷 Use Webcam' reconnect button.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>SQLException</font> (Unique Constraint Violation)", table_cell_bold),
            Paragraph("Student looks at the camera multiple times during the same class session, attempting duplicate attendance.", table_cell_style),
            Paragraph("Pre-checked in <font face='Courier'>AttendanceService</font> via <font face='Courier'>isAttendanceMarked()</font> and backed by relational composite key. Returns <font face='Courier'>ALREADY_MARKED</font>; displays 'ALREADY RECORDED' without crashing.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>SQLException</font> (Database File Locked / Busy)", table_cell_bold),
            Paragraph("Concurrent read/write access collisions on the embedded SQLite database file across background threads.", table_cell_style),
            Paragraph("Enforced strict connection lifecycle management using <font face='Courier'>try-finally</font> blocks with safe closing helpers, and synchronized write blocks on student/attendance DAO operations.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>ClassNotFoundException</font>", table_cell_bold),
            Paragraph("SQLite or MySQL JDBC driver JAR files are missing from the classpath during execution.", table_cell_style),
            Paragraph("Static initializers in <font face='Courier'>DBConnection</font> check driver registration upfront and present actionable GUI error dialogs informing the user of the exact missing JAR file.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>NullPointerException</font>", table_cell_bold),
            Paragraph("Camera hardware bus drops a video frame due to system CPU throttling or USB latency.", table_cell_style),
            Paragraph("Strict non-null guards are positioned at the entrance of the frame processing pipeline; null frames are bypassed seamlessly without disrupting the background capture loop.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>DateTimeParseException</font>", table_cell_bold),
            Paragraph("User inputs malformed date strings in attendance date filter queries.", table_cell_style),
            Paragraph("<font face='Courier'>ValidationUtil</font> utilizes thread-safe <font face='Courier'>DateTimeFormatter</font> patterns (ISO-8601, DD-MM-YYYY, DD/MM/YYYY) with fallback to <font face='Courier'>LocalDate.now()</font>.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>IllegalArgumentException</font>", table_cell_bold),
            Paragraph("Facial bounding box boundaries exceed camera frame dimensions during rapid head movement.", table_cell_style),
            Paragraph("Coordinate clamping algorithms (<font face='Courier'>Math.max(0, ...)</font> and <font face='Courier'>Math.min(width, ...)</font>) normalize bounding rectangles before image subimage cropping.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>IOException</font>", table_cell_bold),
            Paragraph("Operating system disk write permissions denied when writing face crop samples to <font face='Courier'>data/faces/</font>.", table_cell_style),
            Paragraph("The enrollment pipeline creates parent directories automatically via <font face='Courier'>dir.mkdirs()</font> and falls back to database-only feature vector persistence if disk writes are restricted.", table_cell_style)
        ]
    ]

    t_exc = Table(exc_data, colWidths=[100, 130, 250])
    t_exc.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#e2e8f0")),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_exc)

    story.append(PageBreak())

    # ================= SECTION 6: DATABASE SCHEMA =================
    story.append(Paragraph("6. Database Schema &amp; Relational Specifications", h1_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_primary, spaceBefore=2, spaceAfter=12))

    story.append(Paragraph(
        "The relational database schema is normalized to Third Normal Form (3NF) to guarantee zero data anomalies and enforce referential integrity across students, attendance, and biometric features:",
        body_style
    ))

    schema_data = [
        [Paragraph("<b>Table Name</b>", table_header_style), Paragraph("<b>Key Columns &amp; Types</b>", table_header_style), Paragraph("<b>Constraints &amp; Foreign Keys</b>", table_header_style), Paragraph("<b>Purpose</b>", table_header_style)],
        [
            Paragraph("<font face='Courier'>users</font>", table_cell_bold),
            Paragraph("<font face='Courier'>id</font> INT PK<br/><font face='Courier'>username</font> TEXT UNIQUE<br/><font face='Courier'>password_hash</font> TEXT<br/><font face='Courier'>full_name</font> TEXT<br/><font face='Courier'>role</font> TEXT", table_cell_style),
            Paragraph("UNIQUE(username)<br/>DEFAULT role = 'TEACHER'", table_cell_style),
            Paragraph("Stores faculty and administrator authentication credentials secured via SHA-256.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>students</font>", table_cell_bold),
            Paragraph("<font face='Courier'>id</font> INT PK<br/><font face='Courier'>roll_no</font> TEXT UNIQUE<br/><font face='Courier'>name</font> TEXT<br/><font face='Courier'>department</font> TEXT<br/><font face='Courier'>course</font> TEXT<br/><font face='Courier'>email</font> TEXT UNIQUE", table_cell_style),
            Paragraph("UNIQUE(roll_no)<br/>UNIQUE(email)<br/>INDEX idx_department", table_cell_style),
            Paragraph("Stores official student academic records, demographic profiles, and division assignments.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>face_data</font>", table_cell_bold),
            Paragraph("<font face='Courier'>id</font> INT PK<br/><font face='Courier'>student_id</font> INT FK<br/><font face='Courier'>encoding</font> TEXT<br/><font face='Courier'>sample_count</font> INT", table_cell_style),
            Paragraph("FK -> students(id)<br/>ON DELETE CASCADE<br/>INDEX idx_student_face", table_cell_style),
            Paragraph("Stores serialized 1024-dimensional LBPH biometric facial feature vectors.", table_cell_style)
        ],
        [
            Paragraph("<font face='Courier'>attendance</font>", table_cell_bold),
            Paragraph("<font face='Courier'>id</font> INT PK<br/><font face='Courier'>student_id</font> INT FK<br/><font face='Courier'>attendance_date</font> TEXT<br/><font face='Courier'>attendance_time</font> TEXT<br/><font face='Courier'>status</font> TEXT<br/><font face='Courier'>session_id</font> TEXT", table_cell_style),
            Paragraph("FK -> students(id)<br/><b>UNIQUE(student_id, attendance_date, session_id)</b><br/>INDEX idx_attendance_date", table_cell_style),
            Paragraph("Logs validated attendance events with strict duplicate prevention per class session.", table_cell_style)
        ]
    ]

    t_schema = Table(schema_data, colWidths=[70, 140, 130, 140])
    t_schema.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#e2e8f0")),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_schema)

    # ================= SECTION 7: TESTING & VIVA GUIDE =================
    story.append(Spacer(1, 10))
    story.append(Paragraph("7. Verification, Automated Testing &amp; Viva Q&amp;A Guide", h1_style))
    story.append(HRFlowable(width="100%", thickness=1.5, color=c_primary, spaceBefore=2, spaceAfter=12))

    story.append(Paragraph("<b>7.1 Automated Test Execution Results (100% Passing)</b>", h2_style))

    test_data = [
        [Paragraph("<b>Test Suite</b>", table_header_style), Paragraph("<b>Verification Objective</b>", table_header_style), Paragraph("<b>Observed Result</b>", table_header_style), Paragraph("<b>Status</b>", table_header_style)],
        [Paragraph("TEST 1", table_cell_bold), Paragraph("Database Connection (SQLite / PRAGMA)", table_cell_style), Paragraph("Connection verified; PRAGMA foreign_keys = ON active.", table_cell_style), Paragraph("<font color='#16a34a'><b>PASSED</b></font>", table_cell_style)],
        [Paragraph("TEST 2", table_cell_bold), Paragraph("Authentication &amp; SHA-256 Digest", table_cell_style), Paragraph("Admin authenticated successfully with matching hash.", table_cell_style), Paragraph("<font color='#16a34a'><b>PASSED</b></font>", table_cell_style)],
        [Paragraph("TEST 3", table_cell_bold), Paragraph("StudentDAO Query &amp; Mapping", table_cell_style), Paragraph("Retrieved student record with accurate model attributes.", table_cell_style), Paragraph("<font color='#16a34a'><b>PASSED</b></font>", table_cell_style)],
        [Paragraph("TEST 4", table_cell_bold), Paragraph("LBPH 1024-D Extraction &amp; &chi;<sup>2</sup>", table_cell_style), Paragraph("Self-match distance = 0.0000; Identity confidence = 99.4%.", table_cell_style), Paragraph("<font color='#16a34a'><b>PASSED</b></font>", table_cell_style)],
        [Paragraph("TEST 5", table_cell_bold), Paragraph("Biometric Face Enrollment &amp; Cache", table_cell_style), Paragraph("Enrolled multi-sample template; memory cache updated.", table_cell_style), Paragraph("<font color='#16a34a'><b>PASSED</b></font>", table_cell_style)],
        [Paragraph("TEST 6", table_cell_bold), Paragraph("Duplicate Attendance Prevention", table_cell_style), Paragraph("Second consecutive attendance scan blocked (ALREADY_MARKED).", table_cell_style), Paragraph("<font color='#16a34a'><b>PASSED</b></font>", table_cell_style)],
        [Paragraph("TEST 7", table_cell_bold), Paragraph("Dashboard Metrics Aggregation", table_cell_style), Paragraph("Total, Present, Absent counts calculated accurately.", table_cell_style), Paragraph("<font color='#16a34a'><b>PASSED</b></font>", table_cell_style)]
    ]

    t_test = Table(test_data, colWidths=[65, 160, 200, 55])
    t_test.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#e2e8f0")),
        ('GRID', (0,0), (-1,-1), 0.5, c_border),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(t_test)

    story.append(Spacer(1, 10))
    story.append(Paragraph("<b>7.2 Academic Viva Voce Questions &amp; Detailed Answers</b>", h2_style))

    story.append(Paragraph(
        "<b>Q1: Why did you choose LBPH over Deep Learning (e.g., CNN or FaceNet)?</b><br/>"
        "<i>Answer:</i> Convolutional Neural Networks require gigabytes of external framework dependencies (TensorFlow/PyTorch) and dedicated GPU hardware (CUDA), "
        "making them unfeasible for standard classroom desktop PCs. LBPH is lightweight, runs in pure CPU bytecode with under 150 MB RAM, processes video frames at 25 FPS, "
        "and achieves over 99% accuracy on frontal faces without internet connectivity or GPU acceleration.",
        body_style
    ))

    story.append(Paragraph(
        "<b>Q2: How does the system handle poor or uneven lighting in classrooms?</b><br/>"
        "<i>Answer:</i> The facial crop passes through Grayscale Histogram Equalization before feature extraction. This stretches the pixel intensity Cumulative Distribution Function (CDF) "
        "across all 256 grayscale levels, effectively neutralizing directional room glare, lamp reflections, and facial shadows.",
        body_style
    ))

    story.append(Paragraph(
        "<b>Q3: How do you prevent a student from marking attendance multiple times during the same lecture?</b><br/>"
        "<i>Answer:</i> Duplicate prevention is enforced at two separate architectural layers: (1) In the business service layer via timestamp throttling and <font face='Courier'>isAttendanceMarked()</font> checks, "
        "and (2) at the database engine level via a composite unique relational constraint: <font face='Courier'>UNIQUE(student_id, attendance_date, session_id)</font>.",
        body_style
    ))

    story.append(Paragraph(
        "<b>Q4: What happens if a computer does not have a physical webcam during evaluation?</b><br/>"
        "<i>Answer:</i> CampusFace features a Virtual Camera Fallback and an integrated <b>One-Click Test Scanner</b>. Evaluators can select any enrolled student from the dropdown or load a photo file "
        "to test the complete computer vision and attendance recording pipeline with 1 click.",
        body_style
    ))

    # Build PDF with NumberedCanvas for header/footer
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"[PDF Generator] Successfully generated: {filename}")

if __name__ == "__main__":
    out_file = "CampusFace_Project_Documentation.pdf"
    if len(sys.argv) > 1:
        out_file = sys.argv[1]
    build_pdf(out_file)
