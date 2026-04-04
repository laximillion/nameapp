from flask import Flask, redirect, url_for, request, render_template, flash
import os

app = Flask(__name__)
app.secret_key = "secret-key-change-this"

basedir = os.path.abspath(os.path.dirname(__file__))

majors_file = os.path.join(basedir, 'static/txt/all_majors.txt')
minors_file = os.path.join(basedir, 'static/txt/all_minors.txt')


def load_list(file_path):
    items = []
    with open(file_path, "r", encoding="utf-8") as file:
        for line in file:
            clean = line.strip()
            if clean:
                items.append(clean)
    return items


majorslist = load_list(majors_file)
minorslist = load_list(minors_file)


# map dropdown text -> htmltablecs.html section id base
PROGRAM_NAME_MAP = {
    # majors + minors
    "Africana Studies": "Africana",
    "Anthropology": "Anthropology",
    "Architectural Studies": "Arch",
    "Art History": "ArtHist",
    "Art Studio": "ArtStudio",
    "Astronomy": "Astro",
    "Biochemistry": "BioChem",
    "Biological Sciences": "Bio",
    "Chemistry": "Chem",
    "Classical Studies": "ClassicsStudies",
    "Computer Science": "CompSci",
    "Dance": "Dance",
    "Mathematics": "Math",
    "Music": "Music",
    "Philosophy": "Philosophy",
    "Physics": "Physics",
    "Politics": "Politics",
    "Psychology": "Psychology",
    "Religion": "Religion",
    "Romance Languages and Cultures": "RomanceLanguagesCultures",
    "Sociology": "Sociology",
    "Spanish": "Spanish",
    "Statistics": "Statistics",
    "Economics": "Economics",
    "English": "English",
    "Environmental Studies": "EnvironmentalStudies",
    "French": "French",
    "Gender Studies": "GenderStudies",
    "Geography": "Geography",
    "Geology": "Geology",
    "History": "History",
    "Italian": "Italian",

    # minors only / special
    "Arabic": "Arabic",
    "Asian Studies": "Asian",
    "Chinese": "Chinese",
    "Educational Studies": "EducationalStudies",
    "Entrepreneurship, Organizations, and Society": "EntrepreneurshipOrganizationsSociety",
    "Film Media Theater": "FMT",
    "Japanese": "Japanese",
    "Jewish Studies": "JewishStudies",
    "Latin American Studies": "LatinAmericanStudies",

    # special CRPE names
    "Critical Race and Political Economy (CRPE): CRPE Pathway": "CRPEOne",
    "Critical Race and Political Economy (CRPE): Critical Social Thought Pathway": "CRPE-CST",
    "Critical Race and Political Economy (CRPE): Africana Studies Pathway": "CRPE-Africana",
    "Critical Race and Political Economy (CRPE): Latinx Pathway": "CRPE-Latinx",
}


def to_section_name(program_name, program_type):
    """
    Converts dropdown text like 'Computer Science'
    into html section id like 'CompSci Major' or 'CompSci Minor'
    """
    base = PROGRAM_NAME_MAP.get(program_name)

    if not base:
        return None

    return f"{base} {program_type}"


@app.route('/')
def home():
    return render_template(
        'main.html',
        majorslist=majorslist,
        minorslist=minorslist
    )


@app.route('/requirements', methods=['POST'])
def requirements():
    major1 = request.form.get('major1', '').strip()
    major2 = request.form.get('major2', '').strip()
    minor = request.form.get('minor', '').strip()

    major_count = sum(bool(x) for x in [major1, major2])
    minor_count = 1 if minor else 0

    if not major1:
        flash("Please choose your first major.")
        return redirect(url_for('home'))

    # only allowed:
    # 1 major
    # 2 majors
    # 1 major + 1 minor
    if major_count == 2 and minor_count == 1:
        flash(
            "Choose only one of these: one major, two majors, or one major and one minor.")
        return redirect(url_for('home'))

    selected_items = []
    selected_names = []

    if major1:
        section = to_section_name(major1, "Major")
        selected_items.append({
            "name": major1,
            "type": "Major"
        })
        if section:
            selected_names.append(section)

    if major2:
        section = to_section_name(major2, "Major")
        selected_items.append({
            "name": major2,
            "type": "Major"
        })
        if section:
            selected_names.append(section)

    if minor:
        section = to_section_name(minor, "Minor")
        selected_items.append({
            "name": minor,
            "type": "Minor"
        })
        if section:
            selected_names.append(section)

    return render_template(
        'htmltablecs.html',
        selected_items=selected_items,
        selected_names=selected_names
    )


if __name__ == '__main__':
    app.run(debug=True)
